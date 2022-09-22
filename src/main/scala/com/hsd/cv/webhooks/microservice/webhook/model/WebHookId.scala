package com.hsd.cv.webhooks.microservice.webhook.model

import zio.json.*

import java.util.UUID

case class WebHookId(id: Long, url: String, topic: String, format: String, volume: String)

object WebHookId:
  given JsonEncoder[WebHookId] =
    DeriveJsonEncoder.gen[WebHookId]
  given JsonDecoder[WebHookId] =
    DeriveJsonDecoder.gen[WebHookId]
