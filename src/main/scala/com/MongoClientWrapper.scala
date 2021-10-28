package com

import akka.actor.typed.ActorSystem

import org.mongodb.scala._


case class User(_id: String, first_name: String, last_name: String, mail: String, password: String, is_admin: Boolean)

object MongoClientWrapper {

    var actor_system: Option[ActorSystem[_]] = None
    var db: Option[MongoDatabase] = None

    def apply(actor_system: ActorSystem[_]) = {

        val mongo_user: String = "celembrimbor"
        val mongo_pw: String = "okRlmS6wHEgIp6l3"
        val uri: String = "mongodb+srv://" + mongo_user + ":" + mongo_pw + "@dublin1.zuwxd.mongodb.net/estimato?retryWrites=true&w=majority"
        System.setProperty("org.mongodb.async.type", "netty")
        val client: MongoClient = MongoClient(uri)
        
        this.actor_system = Some(actor_system)
        db = Some(client.getDatabase("estimato"))

        //actor_system.log.error("Initialised mongo wrapper properly!")
    }
}
