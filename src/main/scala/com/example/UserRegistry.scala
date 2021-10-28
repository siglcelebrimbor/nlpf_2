package com.example

//#user-registry-actor
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import scala.concurrent.Future

import com.MongoClientWrapper
import org.mongodb.scala._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries._
import scala.util.{Success,Failure}

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

  def apply(): Behavior[Command] = {

   /*
    //val codecRegistry = fromRegistries(fromProviders(classOf[User]), DEFAULT_CODEC_REGISTRY)
    val collec: MongoCollection[User] = MongoClientWrapper.db.get.getCollection("users")//.withCodecRegistry(codecRegistry)
    val users: FindObservable[User] = collec.find
    val get_users: Future[Seq[User]] = users.collect.head
    implicit val ec = scala.concurrent.ExecutionContext.global


    var initial_set: Set[User] = Set.empty
    get_users onComplete {
      case Success(users) => initial_set = users.toSet
      case Failure(t) => initial_set = Set.empty
    }
    //val set: Set[Usq[User]) => all.toSet)er] = users.collect.subscribe((all: Seq[User]) => all.toSet)
    */
    registry(Set.empty)
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
