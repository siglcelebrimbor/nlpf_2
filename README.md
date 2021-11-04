# nlpf_2

# Introduction (how to run this)

### initial ressource:
https://developer.lightbend.com/guides/akka-http-quickstart-scala/

### start the server:

```
> sbt
[info] welcome to sbt 1.4.6 (Oracle Corporation Java 11.0.12)
[info] loading settings for project nlpf_2-build from plugins.sbt ...
[info] loading project definition from /home/victor/epita/nlpf_2/project
[info] loading settings for project root from build.sbt ...
[info] set current project to akka-http-quickstart-scala (in build file:/home/victor/epita/nlpf_2/)
[info] sbt server started at local:///home/victor/.sbt/1.0/server/5fde97bf333621e3c811/sock
[info] started sbt server
> reStart
Multiple main classes detected. Select one to run:
 [1] Main
 [2] com.example.QuickstartApp
 > 2

```


### compile scala front to js:

```
> sbt
> fastLinkJS
```

cela génere un fichier javascript 'main.js' et 'main.js.map' dans target/scala-2.13/akka-http-quickstart-scala-fastopt/


### IDE / DEBUG

L'extension metals sur VS Code offre du debugging scala (très très utile) (attention c'est gourmand en ressource)

# Project structure breakdown

## BACKEND

    * framework: Akka https://akka.io/docs/
    * connection MongoDB: Alpakka https://doc.akka.io/docs/alpakka/current/mongodb.html


fichiers (root= src/main/scala/com):

    * example/QuickstartApp.scala: fichier principal. Lance l'application

    * example/<foo>Registry.scala: Définit l'entité <foo> en tant que Class et les actions élémentaires à effectuer dessus (create, delete, get all, get one, etc...). Gère la connection avec la base de données Mongo

    * example/JsonFormats.scala: définit la méthode de conversion en Json à utiliser pour les types définis dans <foo>Registry.scala
    
    * example/Routes.scala: définit les routes

    * MongoClientWrapper.scala: singleton object gérant la connection avec la base de donnée (utilisé par les fichiers <foo>Registry.scala)

## FRONTEND

    * framework: Scala JS (http://www.scala-js.org/doc/tutorial/basic/)

fichiers (root= src/main/scala/front)

    * main.scala: point d'entrée de l'application


le fichier index.html à la racine du projet contient le script target/scala-2.13/akka-http-quickstart-scala-fastopt/main.js généré par la compilation du front