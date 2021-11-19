package com.example

import com.example.UserRegistry.UserActionPerformed
import com.example.ProjectRegistry.ProjectActionPerformed

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat5(User)
  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val dvfIndicatorJsonFormat = jsonFormat8(DvfIndicator)
  implicit val dvfIndicatorsJsonFormat = jsonFormat1(DvfIndicators)

  implicit val projectJsonFormat = jsonFormat7(Project)
  implicit val projectsJsonFormat = jsonFormat1(Projects)

  implicit val userActionPerformedJsonFormat = jsonFormat1(UserActionPerformed)
  implicit val projectActionPerformedJsonFormat = jsonFormat1(ProjectActionPerformed)

}
//#json-formats
