package com.hsd.cv.webhooks.microservice.webhook.model

object Volume extends Enumeration {
//  type Volume = Value
//  val Full, Short = Value

  protected case class VolumeVal(name: String) extends super.Val {
  }

  import scala.language.implicitConversions
  implicit def valueToVolumeVal(x: Value): VolumeVal = x.asInstanceOf[VolumeVal]

  val Full = VolumeVal("full")
  val Short = VolumeVal("short")

}
