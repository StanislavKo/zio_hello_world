package com.hsd.cv.webhooks.microservice.webhook.repository

import com.hsd.cv.webhooks.microservice.webhook.model.{WebHook, WebHookId}
import io.getquill._
import io.getquill.jdbczio.Quill
import zio._

import javax.sql.DataSource

case class PersistentPostgresqlWebHookRepo(ds: DataSource) extends WebHookRepo {
  val ctx = new PostgresZioJdbcContext(Escape)

  import ctx._

  override def register(webHook: WebHook): Task[Long] = {
    val id = ctx.run {
      quote {
        querySchema[WebHookId]("webhook")
          .insertValue {
            lift(
              WebHookId(
                0,
                webHook.url,
                webHook.topic,
                webHook.format,
                webHook.volume
              )
            )
          }
          .returningGenerated(_.id)
      }
    }
    id
  }.provide(ZLayer.succeed(ds))

  override def lookup(id: Long): Task[Option[WebHookId]] =
    ctx
      .run {
        quote {
          querySchema[WebHookId]("webhook")
            .filter(p => p.id == lift(id))
            .map(wh => WebHookId(wh.id, wh.url, wh.topic, wh.format, wh.volume))
        }
      }
      .provide(ZLayer.succeed(ds))
      .map(_.headOption)

  override def lookupByUrl(url: String): Task[Option[WebHookId]] =
    ctx
      .run {
        quote {
          querySchema[WebHookId]("webhook")
            .filter(p => p.url == lift(url))
            .map(wh => WebHookId(wh.id, wh.url, wh.topic, wh.format, wh.volume))
        }
      }
      .provide(ZLayer.succeed(ds))
      .map(_.headOption)

  override def webhooks: Task[List[WebHookId]] =
    ctx
      .run {
        quote {
          querySchema[WebHookId]("webhook").map(wh =>
            WebHookId(wh.id, wh.url, wh.topic, wh.format, wh.volume)
          )
        }
      }
      .provide(ZLayer.succeed(ds))

  override def delete(id: Long): Task[Unit] =
    ctx
      .run {
        quote {
          querySchema[WebHookId]("webhook")
            .filter(p => p.id == lift(id))
            .delete
        }
      }
      .provide(ZLayer.succeed(ds))
      .map(id => ())
}

object PersistentPostgresqlWebHookRepo {
  def layer: ZLayer[Any, Throwable, PersistentPostgresqlWebHookRepo] =
    Quill.DataSource.fromPrefix("WebHookApp") >>>
      ZLayer.fromFunction(PersistentPostgresqlWebHookRepo(_))
}
