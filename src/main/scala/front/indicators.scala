package indicators

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLImageElement
import org.scalajs.dom.{ document, window } 
import scala.concurrent.Future
import scalatags.JsDom.all._

import scalajs.js.JSON.parse

import scalajs.js
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scalatags.JsDom

//import akka.http.scaladsl.model.{ HttpRequest, HttpMethods, HttpEntity }

final case class Indicator(postal_code: String, year: String, rental_fq_by_sqm: Number, rental_median_by_sqm: Number, rental_tq_by_sqm: Number,
    purchase_tq_by_sqm: Number, purchase_fq_by_sqm: Number, purchase_median_by_sqm: Number)

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

object graphs {


    var indicators: List[Indicator] = List[Indicator]()
    var generatedGraph: Boolean = false
    var fetchedIndicators: Boolean = false
    var buyingPriceGraph: Option[Graph] = None
    var priceEvolutionGraph: Option[Graph] = None

    def getIndicators(postalCode: String) = {
        println("get indicators for code:", postalCode)
        implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
        val res : Future[dom.raw.XMLHttpRequest] = dom.ext.Ajax.get("http://localhost:8080/dvfindicators?postal_code=" + postalCode)

        res.andThen(res => {
            val jsonObject = js.JSON.parse(res.get.responseText)
            //val indicators: Array[scala.scalajs.js.Dynamic] = Array(jsonObject.dvfindicators)
            val indicatorsField = jsonObject.dvfindicators

            indicatorsField.map((elt: js.Dynamic) => {
                val indicator: Indicator = Indicator(
                    elt.postal_code.asInstanceOf[String],
                    elt.year.asInstanceOf[String],
                    elt.rental_fq_by_sqm.asInstanceOf[Number],
                    elt.rental_median_by_sqm.asInstanceOf[Number],
                    elt.rental_tq_by_sqm.asInstanceOf[Number],
                    elt.purchase_fq_by_sqm.asInstanceOf[Number],
                    elt.purchase_median_by_sqm.asInstanceOf[Number],
                    elt.purchase_tq_by_sqm.asInstanceOf[Number]
                    )
                println("will add indicator: ", indicator)
                indicators = indicators ++ List(indicator)
            })
            println("on complete indicators:", indicators)
            fetchedIndicators = true
        })
    }


    def generateBuyingPriceGraph() {
        // generate buying price graphs
        val indicator2020: Option[Indicator] = indicators.find(_.year == "2020")
        val dataStr: String = List(indicator2020.get.purchase_fq_by_sqm.doubleValue(), indicator2020.get.purchase_median_by_sqm.doubleValue(), indicator2020.get.purchase_tq_by_sqm.doubleValue())
                                .sorted
                                .mkString(",")
        val queryStr: String = s"""{type: 'bar',
                                  |data: { labels: ['1st quartile', 'median', '3rd quartile'],
                                  |datasets: [{label: 'buying price quartiles', data: [$dataStr] }]}
                                  |}""".stripMargin.replaceAll("\n", " ")
        buyingPriceGraph = Some(new Graph(queryStr))
    }

    def generatePriceEvolutionGraph() {
        // generate year comparison
        val data: List[Number] = indicators
                    .sortBy(indic => indic.year)
                    .map(indic => indic.purchase_median_by_sqm)
        val dataStr: String = data.mkString(",")
        val queryStr: String = s"""{type: 'line',
                                    |data: { labels: [2016, 2017, 2018, 2019, 2020],
                                            |datasets: [{label: 'buying price (€ / m²)', data: [$dataStr] }]}
                                    |}""".stripMargin.replaceAll("\n", " ")
        priceEvolutionGraph = Some(new Graph(queryStr))
    }

    def generateGraph() {
        if (!generatedGraph) {
            if (!fetchedIndicators) {
                getIndicators("75001")
            }
            else {
                generateBuyingPriceGraph()
                generatePriceEvolutionGraph()
                generatedGraph = true
            }
        }
    }

}



object indicators
{

    def Graphs(): Unit = 
    {
        def render() {
            if (!graphs.priceEvolutionGraph.isEmpty && graphs.priceEvolutionGraph.get.isReady) {
                Console.println("price evolution graph ready!")
                val g = graphs.priceEvolutionGraph.get
                g.getContext.drawImage(g.element, 0, 0, g.getCanvas.width, g.getCanvas.height)
            }
            if (!graphs.buyingPriceGraph.isEmpty && graphs.buyingPriceGraph.get.isReady) {
                val g = graphs.buyingPriceGraph.get
                g.getContext.drawImage(g.element, 0, 0, g.getCanvas.width, g.getCanvas.height)
            }
            
        }

        val renderHandle = dom.window.setInterval(() => render, 1000)
        val generateGraphHandle = dom.window.setInterval(() => graphs.generateGraph, 1000)
        if (graphs.generatedGraph) {
            dom.window.clearInterval(generateGraphHandle)
        }   
    }


    val default = div(cls:="container",
            h1(cls := "title", "Indicators"),
            Graphs())
}