package com.hsd.cv.webhooks.microservice.producer

import zio.*
import zio.kafka.*
import zio.kafka.producer.*

object KafkaProducer {

  val layer: ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped(
      Producer.make(
        ProducerSettings(List("localhost:29092"))
      )
    )
}
