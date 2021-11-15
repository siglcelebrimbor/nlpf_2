import org.scalajs.dom.{ document, window, raw }

object Main {
    def main(args: Array[String]): Unit = {
      println("Hello world!")
      val title: raw.Element = document.createElement("h1")
      title.textContent = "Hello World!"
      document.body.appendChild(title)
    }
}
