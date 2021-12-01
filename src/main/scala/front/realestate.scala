package front

package address

import org.scalajs.dom
import scalatags.JsDom.all._

import scala.util.matching.Regex

import indicators._

object realestateForm {

    def computeScore(): Double = {
        //println("area str:", area.value.toString(), "; price str: ", price.value.toString())
        //println("area:", area.value.toInt, "; price: ", price.value.toInt)
        val dvf2020Indicator: Indicator = graphs.indicators.find(_.year == "2020").get
        val price_per_sqm: Double = price.value.toDouble / area.value.toDouble
        return math.round(price_per_sqm * 100 / dvf2020Indicator.purchase_median_by_sqm.doubleValue())
    }

    def computeRentability(): Double = {
        //val price_per_sqm: Double = area.value.toInt * price.value.toInt
        val dvf2020Indicator: Indicator = graphs.indicators.find(_.year == "2020").get
        val estimated_monthly_rent: Double = dvf2020Indicator.rental_median_by_sqm.doubleValue() * area.value.toDouble
        return math.round(estimated_monthly_rent * 12 * 100 / price.value.toDouble)
    }

    val area = input(
        `type`:="number",
        placeholder:="60m²",
        value:=60
    ).render
    val area_output = span.render

    area.onkeyup = (e: dom.Event) => {
        if (graphs.generatedGraph) {
            score_output.textContent = "Price comparison to market: " + computeScore().toString() + "% of median price"
            rentabilisation_rate_output.textContent = "Rentabilisation Rate: " + computeRentability().toString() + "%"
        }

    }

    val price = input(
        `type`:="number",
        placeholder:="500000€",
        value:=500000
    ).render
    val price_output = span.render

    price.onkeyup = (e: dom.Event) => {
        if (graphs.generatedGraph) {
            score_output.textContent = "Score: " + computeScore().toString() + "% of median price"
            rentabilisation_rate_output.textContent = "Rentabilisation Rate: " + computeRentability.toString() + "%"
        }
    }

    val score_output = span().render
    val rentabilisation_rate_output = span().render

/*    val b = button(
        "Confirm",
        onclick := { () =>
            println(area_output.textContent, price_output.textContent)
            //graphs.code = output.textContent
        }
    ).render*/
    
    var default = div(
        h2("Area (m²)"),
        area,
        h2("Price (€)"),
        price,
        div(score_output),
        div(rentabilisation_rate_output)
    )
}