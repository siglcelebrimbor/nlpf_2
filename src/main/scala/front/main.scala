import org.scalajs.dom
import org.scalajs.dom.{ document, window, raw, html }
import scalatags.JsDom.all._
import scala.scalajs.js.timers.setInterval
import nav._
import indicators._
import address._

object Main {
  def main(args: Array[String]): Unit = {

    val content =
      div(cls:="container",
        nav.default,
        //addressForm.default,
        indicators.default
      )
    val root = document.getElementById("root")
    root.appendChild(content.render)

    //setInterval(1000) {countdown.timer()} //use this if we want the element to refresh
  }
}
