package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import com.example.ProjectRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

//#import-json-formats
//#project-routes-class
class ProjectRoutes(projectRegistry: ActorRef[ProjectRegistry.Command])(implicit val system: ActorSystem[_]) {

  //#project-routes-class
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getProjects(): Future[Projects] =
    projectRegistry.ask(GetProjects)
  def getProject(name: String): Future[GetProjectResponse] =
    projectRegistry.ask(GetProject(name, _))
  def createProject(project: Project): Future[ProjectActionPerformed] =
    projectRegistry.ask(CreateProject(project, _))
  def deleteProject(name: String): Future[ProjectActionPerformed] =
    projectRegistry.ask(DeleteProject(name, _))

  //#all-routes
  //#projects-get-post
  //#projects-get-delete
  val projectRoutes: Route =
    pathPrefix("projects") {
      concat(
        //#projects-get-delete
        pathEnd {
          concat(
            get {
              complete(getProjects())
            },
            post {
              entity(as[Project]) { project =>
                onSuccess(createProject(project)) { performed =>
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        //#projects-get-delete
        //#projects-get-post
        path(Segment) { name =>
          concat(
            get {
              //#retrieve-project-info
              rejectEmptyResponse {
                onSuccess(getProject(name)) { response =>
                  complete(response.maybeProject)
                }
              }
              //#retrieve-project-info
            },
            delete {
              //#projects-delete-logic
              onSuccess(deleteProject(name)) { performed =>
                complete((StatusCodes.OK, performed))
              }
              //#projects-delete-logic
            })
        })
      //#projects-get-delete
    }
  //#all-routes
}
