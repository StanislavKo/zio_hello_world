package com.hsd.cv.webhooks.microservice.webhook.repository

import com.hsd.cv.webhooks.microservice.webhook.model.{WebHook, WebHookId}
import com.hsd.cv.webhooks.microservice.webhook.repository.WebHookRepo
import zio.*

trait WebHookRepo:
  def register(webhook: WebHook): Task[Long]

  def lookup(id: Long): Task[Option[WebHookId]]
  
  def lookupByUrl(url: String): Task[Option[WebHookId]]
  
  def webhooks: Task[List[WebHookId]]

  def delete(id: Long): Task[Unit]

object WebHookRepo:
  def register(webhook: WebHook): ZIO[WebHookRepo, Throwable, Long] =
    ZIO.serviceWithZIO[WebHookRepo](_.register(webhook))

  def lookup(id: Long): ZIO[WebHookRepo, Throwable, Option[WebHookId]] =
    ZIO.serviceWithZIO[WebHookRepo]((d) => d.lookup(id))
  
  def lookupByUrl(url: String): ZIO[WebHookRepo, Throwable, Option[WebHookId]] =
    ZIO.serviceWithZIO[WebHookRepo]((d) => d.lookupByUrl(url))
  
  def webhooks: ZIO[WebHookRepo, Throwable, List[WebHookId]] =
    ZIO.serviceWithZIO[WebHookRepo](_.webhooks)

  def delete(id: Long): ZIO[WebHookRepo, Throwable, Unit] =
    ZIO.serviceWithZIO[WebHookRepo]((d) => d.delete(id))

