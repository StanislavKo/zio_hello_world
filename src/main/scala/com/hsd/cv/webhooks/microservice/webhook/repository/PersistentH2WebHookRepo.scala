package com.hsd.cv.webhooks.microservice.webhook.repository

import com.hsd.cv.webhooks.microservice.webhook.model.WebHook
import com.hsd.cv.webhooks.microservice.webhook.repository.{PersistentH2WebHookRepo, WebHookH2Table}
import io.getquill.*
import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.jdbczio.Quill
import zio.*

import java.util.UUID
import javax.sql.DataSource

case class WebHookH2Table(id: Long, url: String, topic: String, format: String, volume: String)

case class PersistentH2WebHookRepo(ds: DataSource) extends WebHookRepo:
  val ctx = new H2ZioJdbcContext(Escape)

  import ctx.*

  override def register(webHook: WebHook): Task[Long] = {
    for
      id <- Random.nextLong
      _ <- ctx.run {
        quote {
          querySchema[WebHookH2Table]("webhook").insertValue {
            lift(WebHookH2Table(id, webHook.url, webHook.topic, webHook.format, webHook.volume))
          }
        }
      }
    yield id
  }.provide(ZLayer.succeed(ds))

  override def lookup(id: Long): Task[Option[WebHook]] =
    ctx.run {
      quote {
        querySchema[WebHookH2Table]("webhook")
          .filter(p => p.id == lift(id))
          .map(wh => WebHook(wh.url, wh.topic, wh.format, wh.volume))
      }
    }.provide(ZLayer.succeed(ds)).map(_.headOption)

  override def webhooks: Task[List[WebHook]] =
    ctx.run {
      quote {
        querySchema[WebHookH2Table]("webhook").map(wh => WebHook(wh.url, wh.topic, wh.format, wh.volume))
      }
    }.provide(ZLayer.succeed(ds))

  override def delete(id: Long): Task[Unit] =
    ctx.run {
      quote {
        querySchema[WebHookH2Table]("webhook")
          .filter(p => p.id == lift(id))
          .delete
      }
    }.provide(ZLayer.succeed(ds)).map(id => ())

object PersistentH2WebHookRepo:
  def layer: ZLayer[Any, Throwable, PersistentH2WebHookRepo] =
    Quill.DataSource.fromPrefix("WebHookApp") >>>
      ZLayer.fromFunction(PersistentH2WebHookRepo(_))
