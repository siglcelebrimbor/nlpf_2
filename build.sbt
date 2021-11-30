lazy val akkaHttpVersion = "10.2.6"
lazy val akkaVersion    = "2.6.17"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.13.4"
    )),
    name := "akka-http-quickstart-scala",
    libraryDependencies ++= Seq(
      //akka main
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,

      //mongodb connector
      "com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % "3.0.3",
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "org.mongodb.scala" %% "mongo-scala-driver" % "4.2.0",
      
      //"ch.rasc" % "bsoncodec" % "1.0.1",
      "ch.qos.logback"    % "logback-classic"           % "1.2.3",
      
      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.1.4"         % Test,

      //scala JS
      "org.scala-js" %%% "scalajs-dom" % "1.1.0",
      "com.lihaoyi"  %%% "scalatags" % "0.9.1",
      
      "io.circe" %% "circe-config" % "0.8.0",
      "io.circe" %% "circe-core" % "0.13.0",
      "io.circe" %% "circe-fs2" % "0.13.0",
      "io.circe" %% "circe-generic" % "0.13.0",
      "io.circe" %% "circe-parser" % "0.13.0",
      "io.circe" %% "circe-refined" % "0.13.0"
    )

  ).enablePlugins(ScalaJSPlugin)


scalaJSUseMainModuleInitializer := true


import org.scalajs.linker.interface.ModuleInitializer
Compile / scalaJSMainModuleInitializer := Some(ModuleInitializer.mainMethodWithArgs("Main", "main"))
