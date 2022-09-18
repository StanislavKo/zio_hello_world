package com.hsd.cv.webhooks.config

import zio.*
import zio.config.*
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

case class KafkaServerConfig(bootstrapServer: String, groupId: String, topic: String)

object KafkaServerConfig {
  val layer: ZLayer[Any, ReadError[String], KafkaServerConfig] =
    ZLayer {
      read {
        descriptor[KafkaServerConfig].from(
          TypesafeConfigSource.fromResourcePath
            .at(PropertyTreePath.$("KafkaServerConfig"))
        )
      }
    }
}