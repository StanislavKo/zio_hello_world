package com.hsd.cv.webhooks

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.scalatest.flatspec.AnyFlatSpec
import scala.collection.JavaConverters._

import java.time.Duration
import java.util.{Collections, Properties}

class KafkaProducerSpec extends AnyFlatSpec {

  "kafka producer" should "send messages" in {
    new Thread(() => {
      MainApp.startProducer()
    }).start()

    Thread.sleep(10_000)

    assert {
      val props = new Properties()
      props.put("bootstrap.servers", "localhost:29092")
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group-" + System.currentTimeMillis)
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest") // earliest, latest

      val consumer: KafkaConsumer[String, String] = new KafkaConsumer(props)
      consumer.subscribe(Collections.singletonList("topic1"))
      consumer.subscribe(List("topic1").asJava)
      println(s"      consumer.subscription=${consumer.subscription()}")

      val consumerRecords: ConsumerRecords[String, String] = consumer.poll(Duration.ofMillis(10_000))
      if (consumerRecords.count == 0) {
        false
      } else {
        consumerRecords.forEach((record: ConsumerRecord[String, String]) => {
          println(s"      record.key=${record.key}, record.value=${record.value}, record.partition=${record.partition}, record.offset=${record.offset}")
        })
        consumer.commitSync
        true
      }
    }
  }
}
