package com.hsd.cv.webhooks.microservice.webhook.repository

import com.hsd.cv.webhooks.microservice.webhook.model.WebHook
import com.hsd.cv.webhooks.microservice.webhook.repository.InmemoryWebHookRepo
import zio.*

import scala.collection.mutable

  case class InmemoryWebHookRepo(map: Ref[mutable.Map[Long, WebHook]]) extends WebHookRepo:
    def register(webhook: WebHook): UIO[Long] =
      for
        id <- Random.nextLong
        _ <- map.updateAndGet(_ addOne(id, webhook))
      yield id

    def lookup(id: Long): UIO[Option[WebHook]] =
      map.get.map(_.get(id))
     
    def webhooks: UIO[List[WebHook]] =
       map.get.map(_.values.toList)

    def delete(id: Long): UIO[Unit] =
      map.get.map(_.remove(id))

  object InmemoryWebHookRepo {
    def layer: ZLayer[Any, Nothing, InmemoryWebHookRepo] =
      ZLayer.fromZIO(
        Ref.make(mutable.Map.empty[Long, WebHook]).map(new InmemoryWebHookRepo(_))
      )
  }