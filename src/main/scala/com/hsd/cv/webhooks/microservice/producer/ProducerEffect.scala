package com.hsd.cv.webhooks.microservice.producer

import org.apache.kafka.clients.producer.RecordMetadata
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.{RIO, Task, ZIO, ZLayer}

import java.util.Date

object ProducerEffect {
  val effect: RIO[Producer, RecordMetadata] =
    Producer.produce("topic1", s"key2-${System.currentTimeMillis()}", s"value-${new Date()}", Serde.string, Serde.string)
}
