# nlpf_2

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
