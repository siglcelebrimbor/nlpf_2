
/*object Main {
    def main(args: Array[String]): Unit = {
      println("Hello world!")
      val title: raw.Element = document.createElement("h1")
      title.textContent = "Hello World!"
      document.body.appendChild(title)
    }
}*/

import org.scalajs.dom.{ document, window, raw, html }
import scalatags.JsDom.all._
import scala.scalajs.js.timers.setInterval
import nav._
import countdown._

object Main {
  def main(args: Array[String]): Unit = {
    val content =
      div(cls:="container",
        nav.default,
        div(id:="timer", 10)
      )
    val root = document.getElementById("root")
    root.appendChild(content.render)
    setInterval(1000) {countdown.timer()}
  }
}
