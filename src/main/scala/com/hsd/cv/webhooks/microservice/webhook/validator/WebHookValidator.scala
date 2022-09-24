package com.hsd.cv.webhooks.microservice.webhook.validator

import com.hsd.cv.webhooks.microservice.webhook.model.{Format, Volume, WebHook, WebHookId}
import com.hsd.cv.webhooks.microservice.webhook.repository.WebHookRepo
import zio.{Duration, RIO, Schedule, ZIO, ZLayer}

trait WebHookValidatorService {
  def isUrlValid(webhook: WebHook): zio.ZIO[Unit, Throwable, Boolean]
  def isParamsValid(webhook: WebHook): zio.ZIO[Unit, Throwable, Boolean]
}

object WebHookValidatorService {
  // implementation
  case class WebHookValidatorServiceImpl(repo: WebHookRepo)
      extends WebHookValidatorService {

    override def isUrlValid(
        webhook: WebHook
    ): zio.ZIO[Unit, Throwable, Boolean] =
      for {
        existingWebhook <- repo.lookupByUrl(webhook.url)
        result          <- ZIO.succeed(existingWebhook.isEmpty)
      } yield result

    override def isParamsValid(
        webhook: WebHook
    ): zio.ZIO[Unit, Throwable, Boolean] =
      for {
        existingFormat <- ZIO.succeed(Format.values.count(_.name.equals(webhook.format)))
        existingVolume <- ZIO.succeed(Volume.values.count(_.name.equals(webhook.volume)))
        result          <- ZIO.succeed(existingFormat > 0 && existingVolume > 0)
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
