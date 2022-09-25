package com.hsd.cv.webhooks.microservice.webhook.repository

import com.hsd.cv.webhooks.microservice.webhook.model.{WebHook, WebHookId}
import com.hsd.cv.webhooks.microservice.webhook.repository.InmemoryWebHookRepo
import zio._

import scala.collection.mutable

case class InmemoryWebHookRepo(map: Ref[mutable.Map[Long, WebHookId]]) extends WebHookRepo {
  override def register(webhook: WebHook): UIO[Long] =
    for {
      id <- Random.nextLong
      _ <- map.updateAndGet(_ addOne(id, WebHookId(id, webhook.url, webhook.topic, webhook.format, webhook.volume, webhook.description, webhook.desccode)))
    } yield id

  override def registerSlow(webhook: WebHook): Task[Long] = register(webhook)

  override def lookup(id: Long): UIO[Option[WebHookId]] =
    map.get.map(_.get(id))

  override def lookupByUrl(url: String): UIO[Option[WebHookId]] =
    map.get.map(coll => Option[WebHookId](coll.values.toList.filter(_.url.eq(url)).last))

  override def webhooks: UIO[List[WebHookId]] =
    map.get.map(_.values.toList)

  override def webhooksUncommitted: UIO[List[WebHookId]] = webhooks

  override def delete(id: Long): UIO[Unit] =
    map.get.map(_.remove(id))
}

object InmemoryWebHookRepo {
  def layer: ZLayer[Any, Nothing, InmemoryWebHookRepo] =
    ZLayer.fromZIO(
      Ref.make(mutable.Map.empty[Long, WebHookId]).map(new InmemoryWebHookRepo(_))
    )
}