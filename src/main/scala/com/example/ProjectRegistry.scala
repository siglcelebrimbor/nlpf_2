package com.example

//#Project-registry-actor
import akka.{NotUsed, Done}
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import akka.stream.alpakka.mongodb.scaladsl.MongoSource
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.MongoClientWrapper
import com.example.QuickstartApp
import com.mongodb.reactivestreams.client._
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistries._
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros

import scala.collection.immutable
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success
import akka.stream.alpakka.mongodb.scaladsl.MongoSink

//#Project-case-classes
final case class Project(id: String, address: String, city_code: String, city_name: String, location_type: String, sqm_area: Int, value: Int)
final case class Projects(projects: immutable.Seq[Project])
//#Project-case-classes

object ProjectRegistry {
  // actor protocol
  sealed trait Command
  final case class GetProjects(replyTo: ActorRef[Projects]) extends Command
  final case class CreateProject(project: Project, replyTo: ActorRef[ProjectActionPerformed]) extends Command
  final case class GetProject(name: String, replyTo: ActorRef[GetProjectResponse]) extends Command
  final case class DeleteProject(name: String, replyTo: ActorRef[ProjectActionPerformed]) extends Command

  final case class GetProjectResponse(maybeProject: Option[Project])
  final case class ProjectActionPerformed(description: String)

  val provider = Macros.createCodecProviderIgnoreNone[Project]()
  val codec = fromRegistries(fromProviders((provider), DEFAULT_CODEC_REGISTRY))

  implicit val materializer = Materializer(QuickstartApp.system)
  implicit val ec = scala.concurrent.ExecutionContext.global

  def get_project_set(): Set[Project] = {

    QuickstartApp.system.log.info("ENTER GET Project SET METHOD")

    val collec: MongoCollection[Project] = MongoClientWrapper.db.get.withCodecRegistry(codec).getCollection("projects", classOf[Project])    
    val source: Source[Project, NotUsed] = MongoSource(collec.find(classOf[Project]))
    val rows: Future[Seq[Project]] = source.runWith(Sink.seq)

    var initial_set: Set[Project] = null

    rows onComplete {
      case Success(projects) => {
        QuickstartApp.system.log.info("fetched projects:" + Projects.toString())
        initial_set = projects.toSet
      }
      case Failure(t) => {
        QuickstartApp.system.log.info("failure while fetching Projects:" + t.toString())
        initial_set = Set.empty
      }
    }
    Await.result(rows, Duration.Inf)
    while (initial_set == null) { //this is a workaround to Await.result not waiting for callback execution
      //QuickstartApp.system.log.info("waiting...")
    }
    QuickstartApp.system.log.info("done setting initial Project set")
    initial_set
  }

  def create_project(project: Project): Set[Project] = {
    QuickstartApp.system.log.info("ENTER CREATE Project METHOD with value:", project)
    val collec: MongoCollection[Project] = MongoClientWrapper.db.get.withCodecRegistry(codec).getCollection("projects", classOf[Project])    
    val source: Source[Project, NotUsed] = Source.single(project)
    val sink: Sink[Project, Future[Done]] = MongoSink.insertOne(collection = collec)
    //sink.runWith(source)
    //source.runWith(MongoSink.insertOne(collec).seq)
    Set.empty
  }

  def apply(): Behavior[Command] = {
    val Projects: Set[Project] = get_project_set()
    QuickstartApp.system.log.info("WILL ENTER METHOD REGISTRY with Projects:" + Projects.toString())
    registry(Projects)
  }

  private def registry(projects: Set[Project]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetProjects(replyTo) =>
        replyTo ! Projects(projects.toSeq)
        Behaviors.same
      case CreateProject(project, replyTo) =>
        replyTo ! ProjectActionPerformed(s"Creating project ${project}")
        registry(create_project(project))
      case GetProject(id, replyTo) =>
        replyTo ! GetProjectResponse(projects.find(_.id == id))
        Behaviors.same
      case DeleteProject(id, replyTo) =>
        replyTo ! ProjectActionPerformed(s"Deleting project $id")
        registry(projects /*TODO: actually remove the project */)
    }
}
//#Project-registry-actor
