package com.example

import akka.actor.ActorRefFactory
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

import scala.util.Failure
import scala.util.Success
import com.MongoClientWrapper


//#main-class
object QuickstartApp {
  //#start-http-server
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  var system : ActorSystem[Nothing] = null

  //#start-http-server
  def main(args: Array[String]): Unit = {
    //#server-bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>


      MongoClientWrapper(context.system)

      val userRegistryActor = context.spawn(UserRegistry(), "UserRegistryActor")
      context.watch(userRegistryActor)

      val projectRegistryActor = context.spawn(ProjectRegistry(), "ProjectRegistryActor")
      context.watch(projectRegistryActor)
      
      val dvfindicatorRegistryActor = context.spawn(DvfIndicatorRegistry(), "DvfIndicatorRegistryActor")
      context.watch(projectRegistryActor)
      

      val routes: Routes = new Routes(userRegistryActor, projectRegistryActor, dvfindicatorRegistryActor)(context.system)

      startHttpServer(routes.routes)(context.system)

      Behaviors.empty
    }

    this.system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
    //#server-bootstrapping
  }

}
//#main-class
