package com.hsd.cv.webhooks.microservice.webhook.repository

import com.hsd.cv.webhooks.microservice.webhook.repository.WebHookRepo
import com.hsd.cv.webhooks.microservice.webhook.model.WebHook
import zio.*

trait WebHookRepo:
  def register(webhook: WebHook): Task[Long]

  def lookup(id: Long): Task[Option[WebHook]]

  def webhooks: Task[List[WebHook]]

  def delete(id: Long): Task[Unit]

object WebHookRepo:
  def register(webhook: WebHook): ZIO[WebHookRepo, Throwable, Long] =
    ZIO.serviceWithZIO[WebHookRepo](_.register(webhook))

  def lookup(id: Long): ZIO[WebHookRepo, Throwable, Option[WebHook]] =
    ZIO.serviceWithZIO[WebHookRepo]((d) => d.lookup(id))

  def webhooks: ZIO[WebHookRepo, Throwable, List[WebHook]] =
    ZIO.serviceWithZIO[WebHookRepo](_.webhooks)

  def delete(id: Long): ZIO[WebHookRepo, Throwable, Unit] =
    ZIO.serviceWithZIO[WebHookRepo]((d) => d.delete(id))

