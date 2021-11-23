package indicators

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLImageElement
import org.scalajs.dom.{ document, window } 


//https://github.com/vmunier/scalajs-simple-canvas-game/blob/master/src/main/scala/simplegame/SimpleCanvasGame.scala
class Graph(query: String) {

    private val canvas = dom.document.createElement("canvas").asInstanceOf[dom.html.Canvas]
    private val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]


    canvas.height = 500
    canvas.width = 800
    dom.document.body.appendChild(canvas)
    private var ready: Boolean = false

    val element = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
    element.onload = (e: dom.Event) => ready = true
    element.src = "https://quickchart.io/chart?c=" + query

    def getCanvas: dom.html.Canvas = canvas
    def getContext: dom.CanvasRenderingContext2D = ctx
    def isReady: Boolean = ready
}

object indicators
{
    def default(): Unit = 
    {

        val g1 = new Graph("{\"type\": \"bar\", \"data\": {\"labels\": [\"Hello\", \"World\"], \"datasets\": [{\"label\": \"Foo\", \"data\": [1, 2]}]}}")
        
        def render() {
            if (g1.isReady) {
                Console.println("image is ready!")

                g1.getContext.drawImage(g1.element, 0, 0, g1.getCanvas.width, g1.getCanvas.height)
            }
            
        }

        dom.window.setInterval(() => render, 1000)

    }
}