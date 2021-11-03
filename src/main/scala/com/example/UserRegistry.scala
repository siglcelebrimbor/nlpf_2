package com.example

//#user-registry-actor
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import scala.concurrent.{Future, Await}

import com.MongoClientWrapper
//import org.mongodb.scala._

import com.mongodb.reactivestreams.client.{MongoClients, MongoClient, MongoDatabase, MongoCollection}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries._
import scala.util.{Success,Failure}
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.configuration.CodecRegistries
import org.mongodb.scala.bson.codecs.Macros
import scala.concurrent.duration.Duration
import com.mongodb.reactivestreams.client.FindPublisher
import akka.stream.scaladsl.Source
import akka.NotUsed
import akka.stream.alpakka.mongodb.scaladsl.MongoSource
import akka.stream.scaladsl.Sink
import akka.stream.Materializer
import akka.stream.ActorMaterializer
import com.example.QuickstartApp

//#user-case-classes
final case class User(first_name: String, last_name: String, mail: String, password: String, is_admin: Boolean)
final case class Users(users: immutable.Seq[User])
//#user-case-classes

object UserRegistry {
  // actor protocol
  sealed trait Command
  final case class GetUsers(replyTo: ActorRef[Users]) extends Command
  final case class CreateUser(user: User, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetUser(name: String, replyTo: ActorRef[GetUserResponse]) extends Command
  final case class DeleteUser(name: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetUserResponse(maybeUser: Option[User])
  final case class ActionPerformed(description: String)

  def get_user_set(): Set[User] = {

    QuickstartApp.system.log.info("ENTER GET USER SET METHOD")

    val provider = Macros.createCodecProviderIgnoreNone[User]()
    val codec = fromRegistries(fromProviders((provider), DEFAULT_CODEC_REGISTRY))

    val collec: MongoCollection[User] = MongoClientWrapper.db.get.withCodecRegistry(codec).getCollection("users", classOf[User])
    
    val source: Source[User, NotUsed] = MongoSource(collec.find(classOf[User]))

    implicit val materializer = Materializer(QuickstartApp.system)

    val rows: Future[Seq[User]] = source.runWith(Sink.seq)
    QuickstartApp.system.log.info("rows:" + rows.toString())

    //val users: FindPublisher[User] = collec.find

    //val get_users: Future[Seq[User]] = users.collect.head
    implicit val ec = scala.concurrent.ExecutionContext.global
    var initial_set: Set[User] = null

    rows onComplete {
      case Success(users) => {
        QuickstartApp.system.log.info("fetched users:" + rows.toString())
        initial_set = users.toSet
      }
      case Failure(t) => {
        QuickstartApp.system.log.info("failure while fetching users:" + t.toString())
        initial_set = Set.empty
      }
    }

    while (initial_set == null) {
      //QuickstartApp.system.log.info("waiting...")
    }
    QuickstartApp.system.log.info("done setting initial set")

    //Await.result(rows, Duration.Inf)
    
    initial_set
  }

  def apply(): Behavior[Command] = {
    val users: Set[User] = get_user_set()
    QuickstartApp.system.log.info("WILL ENTER METHOD REGISTRY with users:" + users.toString())
    registry(users)
  }

  private def registry(users: Set[User]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! Users(users.toSeq)
        Behaviors.same
      case CreateUser(user, replyTo) =>
        replyTo ! ActionPerformed(s"User ${user.mail} created.")
        registry(users + user)
      case GetUser(mail, replyTo) =>
        replyTo ! GetUserResponse(users.find(_.mail == mail))
        Behaviors.same
      case DeleteUser(mail, replyTo) =>
        replyTo ! ActionPerformed(s"User $mail deleted.")
        registry(users.filterNot(_.mail == mail))
    }
}
//#user-registry-actor
