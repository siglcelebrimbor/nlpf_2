package com.example

//#DvfIndicator-registry-actor
import akka.{NotUsed, Done}
import akka.actor.typed.{ActorRef,Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.alpakka.mongodb.scaladsl.MongoSource
import akka.stream.scaladsl.{Sink, Source}
import com.MongoClientWrapper
import com.example.QuickstartApp
import com.mongodb.reactivestreams.client._
import org.bson.codecs.configuration.{CodecRegistries, CodecRegistry}
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros

import scala.collection.immutable
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.util.{Failure,Success}
import akka.stream.alpakka.mongodb.scaladsl.MongoSink
import org.mongodb.scala.model.Filters
import org.bson.conversions.Bson

//#DvfIndicator-case-classes
final case class DvfIndicator(year: String, postal_code: String,
                      rental_fq_by_sqm: Double, rental_tq_by_sqm: Double, rental_median_by_sqm: Double,
                      purchase_fq_by_sqm: Double, purchase_tq_by_sqm: Double, purchase_median_by_sqm: Double)
final case class DvfIndicators(dvfindicators: immutable.Seq[DvfIndicator])
//#DvfIndicator-case-classes

object DvfIndicatorRegistry {
  // actor protocol
  sealed trait Command
  final case class GetDvfIndicators(replyTo: ActorRef[DvfIndicators]) extends Command
  final case class GetDvfIndicator(name: String, replyTo: ActorRef[GetDvfIndicatorResponse]) extends Command

  final case class GetDvfIndicatorResponse(maybeDvfIndicator: Option[DvfIndicator])

  val provider = Macros.createCodecProviderIgnoreNone[DvfIndicator]()
  val codec = fromRegistries(fromProviders((provider), DEFAULT_CODEC_REGISTRY))

  implicit val materializer = Materializer(QuickstartApp.system)
  implicit val ec = scala.concurrent.ExecutionContext.global

  def get_dvfindicator_set(): Set[DvfIndicator] = {
    val collec: MongoCollection[DvfIndicator] = MongoClientWrapper.db.get.withCodecRegistry(codec).getCollection("dvf_indicators", classOf[DvfIndicator])    
    val source: Source[DvfIndicator, NotUsed] = MongoSource(collec.find(classOf[DvfIndicator]))
    val rows: Future[Seq[DvfIndicator]] = source.runWith(Sink.seq)

    var initial_set: Set[DvfIndicator] = null

    rows onComplete {
      case Success(dvfindicators) => {
        QuickstartApp.system.log.info("fetched DvfIndicators:" + dvfindicators.toString())
        initial_set = dvfindicators.toSet
      }
      case Failure(t) => {
        QuickstartApp.system.log.info("failure while fetching DvfIndicators:" + t.toString())
        initial_set = Set.empty
      }
    }
    Await.result(rows, Duration.Inf)
    while (initial_set == null) { //this is a workaround to Await.result not waiting for callback execution
      //QuickstartApp.system.log.info("waiting...")
    }
    //QuickstartApp.system.log.info("done setting initial DvfIndicator set")
    initial_set
  }


  def apply(): Behavior[Command] = {
    val dvfindicators: Set[DvfIndicator] = get_dvfindicator_set()
    QuickstartApp.system.log.info("WILL ENTER METHOD REGISTRY with DvfIndicators:" + dvfindicators.toString())
    registry(dvfindicators)
  }

  private def registry(dvfindicators: Set[DvfIndicator]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetDvfIndicators(replyTo) =>
        replyTo ! DvfIndicators(dvfindicators.toSeq)
        Behaviors.same
      case GetDvfIndicator(postal_code, replyTo) =>
        replyTo ! GetDvfIndicatorResponse(dvfindicators.find(_.postal_code == postal_code))
        Behaviors.same
    }
}
//#DvfIndicator-registry-actor
