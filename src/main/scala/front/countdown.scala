package countdown

import org.scalajs.dom.{ document, window } 

object countdown
{
    def timer(): Unit = 
    {
        var time: Int = document .getElementById("timer").innerHTML.toInt
        if (time > 0)
        {
            time -= 1
        }
        else
        {
            window.alert("Hello World Two!")
            time = 11
        }
        document.getElementById("timer").innerHTML = time.toString;
    }
}