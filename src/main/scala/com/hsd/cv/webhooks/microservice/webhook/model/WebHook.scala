package com.hsd.cv.webhooks.microservice.webhook.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class WebHook(url: String, topic: String, format: String, volume: String, description: String, desccode: String)

object WebHook {
  implicit val encoder: JsonEncoder[WebHook] =
    DeriveJsonEncoder.gen[WebHook]
  implicit val decoder: JsonDecoder[WebHook] =
    DeriveJsonDecoder.gen[WebHook]
}