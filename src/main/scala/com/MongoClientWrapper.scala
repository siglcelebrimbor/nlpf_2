package com

import akka.actor.typed.ActorSystem

//import org.mongodb.scala._
import com.mongodb.reactivestreams.client.{MongoClients, MongoClient, MongoDatabase}
import org.mongodb.scala.{MongoCredential, MongoClientSettings, ServerAddress}
import scala.collection.JavaConverters._
import com.example.QuickstartApp


object MongoClientWrapper {

    var actor_system: Option[ActorSystem[_]] = None
    var db: Option[MongoDatabase] = None

    def apply(actor_system: ActorSystem[_]) = {

        System.setProperty("org.mongodb.async.type", "netty")

        val mongo_user: String = "celebrimbor"
        val mongo_pw: String = "celebrimbor"
        val uri: String = "mongodb+srv://" + mongo_user + ":" + mongo_pw + "@dublin1.zuwxd.mongodb.net/estimato?retryWrites=true&w=majority&authSource=admin&authMechanism=SCRAM-SHA-1"
        
        val client: MongoClient = MongoClients.create(uri)
        
        this.actor_system = Some(actor_system)
        db = Some(client.getDatabase("estimato"))
        actor_system.log.error("Initialised mongo wrapper")
    }
}
