package com.hsd.cv.webhooks

import com.hsd.cv.webhooks.config.{HttpServerConfig, KafkaServerConfig}
import com.hsd.cv.webhooks.microservice.confirmation.ConfirmationApp
import com.hsd.cv.webhooks.microservice.consumer.ConsumerService
import com.hsd.cv.webhooks.microservice.producer.{KafkaProducer, ProducerEffect}
import com.hsd.cv.webhooks.microservice.webhook.WebHookApp
import com.hsd.cv.webhooks.microservice.webhook.repository.{InmemoryWebHookRepo, PersistentH2WebHookRepo, PersistentPostgresqlWebHookRepo}
import com.hsd.cv.webhooks.microservice.webhook.validator.WebHookValidatorService
import zhttp.service.Server
import zio.*

object MainApp {

  def startHttp() = {
    ZIOAppDefault
      .fromZIO(
        ZIO
          .service[HttpServerConfig]
          .flatMap { config =>
            Server.start(
              port = config.port,
              http = WebHookApp() ++ ConfirmationApp()
            )
          }
          .provide(
            WebHookValidatorService.layer,
              // InmemoryWebHookRepo.layer,
            // PersistentH2WebHookRepo.layer,
            PersistentPostgresqlWebHookRepo.layer,
            HttpServerConfig.layer,
            WebHookApp.layerUnit
          )
      )
      .main(Array.empty)
  }

  def startProducer() = {
    ZIOAppDefault
      .fromZIO(
        ProducerEffect.effect
          .provide(KafkaProducer.layer)
          .repeat(Schedule.fixed(Duration.fromSeconds(1)))
      )
      .main(Array.empty)
  }
  
  def startConsumer() = {
    ZIOAppDefault
      .fromZIO(
        ZIO
          .service[ConsumerService]
          .flatMap { _.consume() }
          .provide(
            PersistentPostgresqlWebHookRepo.layer,
            KafkaServerConfig.layer,
            ConsumerService.layer,
            ConsumerService.layerUnit
          )
      )
      .main(Array.empty)
  }

  @main def main() = {
    println("STARTING")

    new Thread(() => {
      startHttp()
    }).start()
    println("STARTED http")

    new Thread(() => {
      startProducer()
    }).start()
    println("STARTED producer")

    new Thread(() => {
      startConsumer()
    }).start()
    println("STARTED consumer")

    Thread.sleep(Long.MaxValue)
    println("END")
  }

}
