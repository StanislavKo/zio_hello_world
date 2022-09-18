package com.hsd.cv.webhooks.microservice.webhook.model

import com.hsd.cv.webhooks.microservice.webhook.model.WebHook
import zio.json.*

import java.util.UUID

case class WebHook(url: String, topic: String, format: String, volume: String)

object WebHook:
  given JsonEncoder[WebHook] =
    DeriveJsonEncoder.gen[WebHook]
  given JsonDecoder[WebHook] =
    DeriveJsonDecoder.gen[WebHook]
