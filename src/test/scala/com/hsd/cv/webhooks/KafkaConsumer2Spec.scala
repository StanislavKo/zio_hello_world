package com.hsd.cv.webhooks

import com.hsd.cv.webhooks.config.KafkaServerConfig
import com.hsd.cv.webhooks.microservice.consumer.ConsumerService
import com.hsd.cv.webhooks.microservice.webhook.repository.{InmemoryWebHookRepo, WebHookRepo}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.scalatest.flatspec.AnyFlatSpec
import zio.{Cause, Exit, Unsafe, ZIO, ZLayer}

import java.util.Properties

class KafkaConsumer2Spec extends AnyFlatSpec {

  "kafka consumer" should "receive messages" in {
    new Thread(() => {
      println(s"######### Thread producer")
      val props = new Properties()
      props.put("bootstrap.servers", "localhost:29092")
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group-" + System.currentTimeMillis)
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest") // earliest, latest

      val producer = new KafkaProducer[String, String](props)
      val topic = "topic3"
      try {
        for (i <- 0 to 20) {
          val record = new ProducerRecord[String, String](topic, "key-" + i, "value-" + i)
          val metadata = producer.send(record)
          println(s"metadata=$metadata")
          Thread.sleep(1000)
        }
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {
        producer.close()
      }
      println("finished")
    }).start()

    val kafkaServerConfigLayer: ZLayer[Any, Unit, KafkaServerConfig] =
      ZLayer {
        ZIO.succeed(KafkaServerConfig("localhost:29092", "groudId" + System.currentTimeMillis(), "topic3"))
      }

    val repoLayer: ZLayer[Any, Nothing, InmemoryWebHookRepo] = InmemoryWebHookRepo.layer

    val consumerLayer: ZLayer[KafkaServerConfig with WebHookRepo, Throwable, ConsumerService] = ConsumerService.layer

    val unitLayer: ZLayer[KafkaServerConfig, Throwable, Unit] = ConsumerService.layerUnit

    assert {

      val result = Unsafe.unsafe(implicit unsafe =>
        zio.Runtime.default.unsafe.run(
          ZIO
            .service[ConsumerService]
            .withParallelism(5)
            .flatMap { cs =>
              cs.consume().fork *>
                ZIO.succeed(Thread.sleep(10_000)) *>
                ZIO.succeed(cs.getHandledMessages())
            }
            .provide(
              repoLayer,
              kafkaServerConfigLayer,
              consumerLayer,
              unitLayer
            )
        )
      )
      println(s"######### result=$result")
      result match {
        case Exit.Success(value) =>
          value > 0
        case Exit.Failure(cause) =>
          false
      }
    }
  }
}
