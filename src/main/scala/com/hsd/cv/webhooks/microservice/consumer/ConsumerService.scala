package com.hsd.cv.webhooks.microservice.consumer

import com.hsd.cv.webhooks.config.KafkaServerConfig
import com.hsd.cv.webhooks.microservice.webhook.model.WebHook
import com.hsd.cv.webhooks.microservice.webhook.repository.WebHookRepo
import zio.ZLayer.FunctionConstructor
import zio.kafka.consumer.Consumer.{AutoOffsetStrategy, OffsetRetrieval}
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde
import zio.{Console, Duration, RIO, Schedule, Task, URIO, ZIO, ZLayer}

import java.io.{DataOutputStream, IOException}
import java.net.{HttpURLConnection, URL, URLEncoder}
import java.util.Date
import scala.io.Source

trait ConsumerService {
  def consume(): zio.ZIO[Unit, Throwable, Long]
}

object ConsumerService {
  // implementation
  case class ConsumerServiceImpl(config: KafkaServerConfig, repo: WebHookRepo)
      extends ConsumerService {

    val effect: RIO[Any, Unit] =
      Consumer.consumeWith(
        settings = ConsumerSettings(config.bootstrapServer :: Nil)
          .withGroupId(config.groupId)
          .withOffsetRetrieval(OffsetRetrieval.Auto(AutoOffsetStrategy.Latest)),
        subscription = Subscription.topics(config.topic),
        keyDeserializer = Serde.string,
        valueDeserializer = Serde.string
      )((k, v) => handle(k, v))

    def sendConfirmation(urlStr: String, k: String, v: String) = {
      try {
        val url = new URL(
          s"$urlStr?k=${URLEncoder.encode(k, "UTF-8")}&v=${URLEncoder.encode(v, "UTF-8")}"
        )
        val con: HttpURLConnection =
          url.openConnection.asInstanceOf[HttpURLConnection]
        con.setConnectTimeout(2000)
        con.setReadTimeout(2000)
        con.setRequestMethod("GET")
        val inputStream = con.getInputStream
        val content = Source.fromInputStream(inputStream).mkString
        if (inputStream != null) inputStream.close()
      } catch {
        case e: IOException => {
          e.printStackTrace()
        }
      }
      ZIO.succeed(() => ())
    }

    def handle(k: String, v: String) = {
      println(s"consumer k=${k}, v=${v}")
      repo.webhooks.flatMap(
        webhooks => ZIO.succeed({
          webhooks.map(wh => sendConfirmation(wh.url, k, v))
          ()
        })
      ).!
    }

    override def consume(): zio.ZIO[Unit, Throwable, Long] =
      effect.repeat(Schedule.fixed(Duration.fromSeconds(5)))
  }

  // layer
  val layer
      : ZLayer[KafkaServerConfig with WebHookRepo, Throwable, ConsumerService] =
    ZLayer {
      for {
        config <- ZIO.service[KafkaServerConfig]
        repo   <- ZIO.service[WebHookRepo]
      } yield ConsumerServiceImpl(config, repo)
    }

  val layerUnit: ZLayer[KafkaServerConfig, Throwable, Unit] =
    ZLayer.fromFunction(() => ())

}
