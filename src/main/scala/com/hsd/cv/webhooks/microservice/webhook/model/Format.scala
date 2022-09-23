package com.hsd.cv.webhooks.microservice.webhook.model

object Format extends Enumeration {
//  type Format = Value
//  val Csv, Json = Value

  protected case class FormatVal(i: Int, name: String, desc: String) extends super.Val(i, name) {
  }

  import scala.language.implicitConversions
  implicit def valueToFormatVal(x: Value): FormatVal = x.asInstanceOf[FormatVal]

  val Csv = FormatVal(1, "csv", "for data scientists")
  val Json = FormatVal(2, "json", "for analytics")

}
