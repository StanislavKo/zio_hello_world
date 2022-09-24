package com.hsd.cv.webhooks.microservice.webhook.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class WebHookId(id: Long, url: String, topic: String, format: String, volume: String)

object WebHookId {
  implicit val encoder: JsonEncoder[WebHookId] =
    DeriveJsonEncoder.gen[WebHookId]
  implicit val decoder: JsonDecoder[WebHookId] =
    DeriveJsonDecoder.gen[WebHookId]
}