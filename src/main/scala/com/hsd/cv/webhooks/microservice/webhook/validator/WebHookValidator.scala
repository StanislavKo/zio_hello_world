package com.hsd.cv.webhooks.microservice.webhook.validator

import com.hsd.cv.webhooks.microservice.webhook.model.{WebHook, WebHookId}
import com.hsd.cv.webhooks.microservice.webhook.repository.WebHookRepo
import zio.{Duration, RIO, Schedule, ZIO, ZLayer}

trait WebHookValidatorService {
  def validate(webhook: WebHook): zio.ZIO[Unit, Throwable, Boolean]
}

object WebHookValidatorService {
  // implementation
  case class WebHookValidatorServiceImpl(repo: WebHookRepo)
      extends WebHookValidatorService {

    override def validate(
        webhook: WebHook
    ): zio.ZIO[Unit, Throwable, Boolean] =
      for {
        existingWebhook <- repo.lookupByUrl(webhook.url)
        result          <- ZIO.succeed(existingWebhook.nonEmpty)
      } yield result
  }

  // layer
  val layer: ZLayer[WebHookRepo, Throwable, WebHookValidatorService] =
    ZLayer {
      for {
        repo <- ZIO.service[WebHookRepo]
      } yield WebHookValidatorServiceImpl(repo)
    }

  val layerUnit: ZLayer[WebHookRepo, Throwable, Unit] =
    ZLayer.fromFunction(() => ())

}
