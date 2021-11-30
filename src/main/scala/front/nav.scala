package nav

import scalatags.JsDom.all._
import address.addressForm

object nav {
  val default =
    div(cls := "nav",
      h1(cls := "title", "Estimato"),
      addressForm.default
    )
}
