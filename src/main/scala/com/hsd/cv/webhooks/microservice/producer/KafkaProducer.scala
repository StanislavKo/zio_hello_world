package com.hsd.cv.webhooks.microservice.producer

import zio._
import zio.kafka._
import zio.kafka.producer._

object KafkaProducer {

  val layer: ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped(
      Producer.make(
        ProducerSettings(List("localhost:29092"))
      )
    )
}
