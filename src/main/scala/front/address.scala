package address

import org.scalajs.dom
import scalatags.JsDom.all._

import scala.util.matching.Regex

import indicators._

object addressForm {

    val addressInput = input(
        `type`:="text",
        placeholder:="8 Rue du Général de Gaulle 75003 Paris"
    ).render

    val output = span.render

    addressInput.onkeyup = (e: dom.Event) => {
        val pattern = """(\d\d\d\d\d)""".r.unanchored
        output.textContent =
            addressInput.value match {
                case pattern(code) => code
                case _ => "75001"
            }
    }
    val b = button(
        "Confirm",
        onclick := { () =>
            println(output.textContent)
            graphs.code = output.textContent
        }
    ).render
    
    var default = div(
        h2("Street Address"),
        addressInput,
        b
    )
}