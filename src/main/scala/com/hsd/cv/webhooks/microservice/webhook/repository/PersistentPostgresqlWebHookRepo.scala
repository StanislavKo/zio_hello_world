package com.hsd.cv.webhooks.microservice.webhook.repository

import com.hsd.cv.webhooks.microservice.webhook.model.WebHook
import com.hsd.cv.webhooks.microservice.webhook.repository.{PersistentPostgresqlWebHookRepo, WebHookTable}
import io.getquill.*
import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.jdbczio.Quill
import zio.*

import java.util.UUID
import javax.sql.DataSource

case class WebHookTable(id: Long, url: String, topic: String, format: String, volume: String)

case class PersistentPostgresqlWebHookRepo(ds: DataSource) extends WebHookRepo:
  val ctx = new PostgresZioJdbcContext(Escape)

  import ctx.*

  override def register(webHook: WebHook): Task[Long] = {
    val id = ctx.run {
      quote {
        querySchema[WebHookTable]("webhook").insertValue {
          lift(WebHookTable(0, webHook.url, webHook.topic, webHook.format, webHook.volume))
        }.returningGenerated(_.id)
      }
    }
    id
  }.provide(ZLayer.succeed(ds))

  override def lookup(id: Long): Task[Option[WebHook]] =
    ctx.run {
      quote {
        querySchema[WebHookTable]("webhook")
          .filter(p => p.id == lift(id))
          .map(wh => WebHook(wh.url, wh.topic, wh.format, wh.volume))
      }
    }.provide(ZLayer.succeed(ds)).map(_.headOption)

  override def webhooks: Task[List[WebHook]] =
    ctx.run {
      quote {
        querySchema[WebHookTable]("webhook").map(wh => WebHook(wh.url, wh.topic, wh.format, wh.volume))
      }
    }.provide(ZLayer.succeed(ds))

  override def delete(id: Long): Task[Unit] =
    ctx.run {
      quote {
        querySchema[WebHookTable]("webhook")
          .filter(p => p.id == lift(id))
          .delete
      }
    }.provide(ZLayer.succeed(ds)).map(id => ())

object PersistentPostgresqlWebHookRepo:
  def layer: ZLayer[Any, Throwable, PersistentPostgresqlWebHookRepo] =
    Quill.DataSource.fromPrefix("WebHookApp") >>>
      ZLayer.fromFunction(PersistentPostgresqlWebHookRepo(_))
