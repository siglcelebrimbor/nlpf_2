package com.example

//#user-registry-actor
import akka.NotUsed
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
import com.mongodb.reactivestreams.client.FindPublisher
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistries._
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros

import scala.collection.immutable
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success

//#user-case-classes
final case class User(first_name: String, last_name: String, mail: String, password: String, is_admin: Boolean)
final case class Users(users: immutable.Seq[User])
//#user-case-classes

object UserRegistry {
  // actor protocol
  sealed trait Command
  final case class GetUsers(replyTo: ActorRef[Users]) extends Command
  final case class CreateUser(user: User, replyTo: ActorRef[UserActionPerformed]) extends Command
  final case class GetUser(name: String, replyTo: ActorRef[GetUserResponse]) extends Command
  final case class DeleteUser(name: String, replyTo: ActorRef[UserActionPerformed]) extends Command

  final case class GetUserResponse(maybeUser: Option[User])
  final case class UserActionPerformed(description: String)

  val provider = Macros.createCodecProviderIgnoreNone[User]()
  val codec = fromRegistries(fromProviders((provider), DEFAULT_CODEC_REGISTRY))

  implicit val materializer = Materializer(QuickstartApp.system)
  implicit val ec = scala.concurrent.ExecutionContext.global

  def get_user_set(): Set[User] = {

    QuickstartApp.system.log.info("ENTER GET USER SET METHOD")

    val collec: MongoCollection[User] = MongoClientWrapper.db.get.withCodecRegistry(codec).getCollection("users", classOf[User])    
    val source: Source[User, NotUsed] = MongoSource(collec.find(classOf[User]))
    val rows: Future[Seq[User]] = source.runWith(Sink.seq)

    var initial_set: Set[User] = null

    rows onComplete {
      case Success(users) => {
        QuickstartApp.system.log.info("fetched users:" + users.toString())
        initial_set = users.toSet
      }
      case Failure(t) => {
        QuickstartApp.system.log.info("failure while fetching users:" + t.toString())
        initial_set = Set.empty
      }
    }
    Await.result(rows, Duration.Inf)
    while (initial_set == null) { //this is a workaround to Await.result not waiting for callback execution
      //QuickstartApp.system.log.info("waiting...")
    }
    QuickstartApp.system.log.info("done setting initial user set")
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
        replyTo ! UserActionPerformed(s"User ${user.mail} created.")
        registry(users + user)
      case GetUser(mail, replyTo) =>
        replyTo ! GetUserResponse(users.find(_.mail == mail))
        Behaviors.same
      case DeleteUser(mail, replyTo) =>
        replyTo ! UserActionPerformed(s"User $mail deleted.")
        registry(users.filterNot(_.mail == mail))
    }
}
//#user-registry-actor
