package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import com.example.UserRegistry._
import com.example.ProjectRegistry._
import com.example.DvfIndicatorRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout



//#import-json-formats
//#user-routes-class
class Routes(userRegistry: ActorRef[UserRegistry.Command],
  projectRegistry: ActorRef[ProjectRegistry.Command],
  dvfindicatorRegistry: ActorRef[DvfIndicatorRegistry.Command])
  (implicit val system: ActorSystem[_]) {

  //#user-routes-class
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getUsers(): Future[Users] =
    userRegistry.ask(GetUsers)
  def getUser(name: String): Future[GetUserResponse] =
    userRegistry.ask(GetUser(name, _))

  def getProjects(): Future[Projects] =
    projectRegistry.ask(GetProjects)
  def getProject(name: String): Future[GetProjectResponse] =
    projectRegistry.ask(GetProject(name, _))
  def createProject(project: Project): Future[ProjectActionPerformed] =
    projectRegistry.ask(CreateProject(project, _))

  def getDvfIndicators(): Future[DvfIndicators] =
    dvfindicatorRegistry.ask(GetDvfIndicators)
  def getDvfIndicator(postal_code: String): Future[GetDvfIndicatorResponse] =
    dvfindicatorRegistry.ask(GetDvfIndicator(postal_code, _))


    private val cors = new com.CORSHandler {}

  //#all-routesClick to add text​
  //#users-get-post
  //#users-get-delete
  val userRoutes: Route =
    pathPrefix("users") {
      concat(
        //#users-get-delete
        pathEnd {
            get {
              cors.corsHandler(complete(getUsers()))
            }
        },
        //#users-get-post
        path(Segment) { name =>
            get {
              //#retrieve-user-info
              rejectEmptyResponse {
                onSuccess(getUser(name)) { response =>
                  cors.corsHandler(complete(response.maybeUser))
                }
              }
              //#retrieve-user-info
            }
        })
    }


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
                  cors.corsHandler(complete((StatusCodes.Created, performed)))
                }
              }
            })
        },
        //#projects-get-delete
        //#projects-get-post
        path(Segment) { name =>
            get {
              //#retrieve-project-info
              rejectEmptyResponse {
                onSuccess(getProject(name)) { response =>
                  cors.corsHandler(complete(response.maybeProject))
                }
              }
              //#retrieve-project-info
            }
        })
      //#projects-get-delete
    }



  val dvfIndicatorRoutes: Route =
    pathPrefix("dvfindicators") {
      concat(
        pathEnd {
          get {
              cors.corsHandler(complete(getDvfIndicators()))
          }
        },
        path(Segment) { postal_code =>
          get {
            //#retrieve-project-info
            rejectEmptyResponse {
              onSuccess(getDvfIndicator(postal_code)) { response =>
                cors.corsHandler(complete(response.maybeDvfIndicator))
              }
            }
            //#retrieve-project-info
          }
      })
    }


  val routes: Route =
    concat(
      userRoutes,
      projectRoutes,
      dvfIndicatorRoutes
    )
}
