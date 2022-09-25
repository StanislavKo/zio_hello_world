package com.hsd.cv.webhooks.microservice.webhook.repository

import com.hsd.cv.webhooks.microservice.webhook.model.{WebHook, WebHookId}
import com.hsd.cv.webhooks.microservice.webhook.repository.PersistentH2WebHookRepo
import io.getquill._
import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.jdbczio.Quill
import zio._

import java.sql.{Connection, DatabaseMetaData}
import java.util.UUID
import javax.sql.DataSource
import scala.collection.mutable.ListBuffer

case class PersistentH2WebHookRepo(ds: DataSource) extends WebHookRepo {
  val ctx = new H2ZioJdbcContext(Escape)

  import ctx._

  override def register(webHook: WebHook): Task[Long] = {
    for {
      id <- Random.nextLong
      _ <- ctx.run {
        quote {
          querySchema[WebHookId]("webhook").insertValue {
            lift(WebHookId(id, webHook.url, webHook.topic, webHook.format, webHook.volume, webHook.description, webHook.desccode))
          }
        }
      }
    } yield id
  }.provide(ZLayer.succeed(ds))

  override def registerSlow(webHook: WebHook): Task[Long] = {
    for {
      idNew <- Random.nextLong
      idNew2 <- ctx.transaction {
        ctx.run {
          quote {
            querySchema[WebHookId]("webhook")
              .insertValue {
                lift(
                  WebHookId(
                    idNew,
                    webHook.url,
                    webHook.topic,
                    webHook.format,
                    webHook.volume,
                    webHook.description,
                    webHook.desccode
                  )
                )
              }
          }
        } *>
        ZIO.succeed(Thread.sleep(10_000)) *>
        ZIO.succeed(idNew)
      }
        .provide(ZLayer.succeed(ds))
    } yield idNew2
  }

  override def lookup(id: Long): Task[Option[WebHookId]] =
    ctx.run {
      quote {
        querySchema[WebHookId]("webhook")
          .filter(p => p.id == lift(id))
          .map(wh => WebHookId(wh.id, wh.url, wh.topic, wh.format, wh.volume, wh.description, wh.desccode))
      }
    }.provide(ZLayer.succeed(ds)).map(_.headOption)

  override def lookupByUrl(url: String): Task[Option[WebHookId]] =
    ctx.run {
      quote {
        querySchema[WebHookId]("webhook")
          .filter(p => p.url == lift(url))
          .map(wh => WebHookId(wh.id, wh.url, wh.topic, wh.format, wh.volume, wh.description, wh.desccode))
      }
    }.provide(ZLayer.succeed(ds)).map(_.headOption)

  override def webhooks: Task[List[WebHookId]] =
    ctx.run {
      quote {
        querySchema[WebHookId]("webhook").map(wh => WebHookId(wh.id, wh.url, wh.topic, wh.format, wh.volume, wh.description, wh.desccode))
      }
    }.provide(ZLayer.succeed(ds))

  override def webhooksUncommitted: Task[List[WebHookId]] = {
    ZIO
      .environment[DataSource]
      .flatMap(env =>
        for {
          list <- {
            val con: Connection = env.get(Tag[Environment]).getConnection
//            val dbmd: DatabaseMetaData = con.getMetaData
//            println(
//              s"DatabaseMetaData.isolations_1=${dbmd.supportsTransactionIsolationLevel(1)}"
//            )
            con.createStatement().executeUpdate("SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL READ UNCOMMITTED")
            val stmt = con.createStatement()
            val rs = stmt.executeQuery("select * from webhook")
            val webhooks = new ListBuffer[WebHookId]
            while (rs.next()) {
              val webhook = WebHookId(
                rs.getLong("id"),
                rs.getString("url"),
                rs.getString("topic"),
                rs.getString("format"),
                rs.getString("volume"),
                rs.getString("description"),
                rs.getString("desccode")
              )
              webhooks.addOne(webhook)
            }
            con.createStatement().executeUpdate("SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL SERIALIZABLE")
            ZIO.succeed(webhooks.toList)
          }
        } yield list
      )
      .provide(ZLayer.succeed(ds))
  }

  override def delete(id: Long): Task[Unit] =
    ctx.run {
      quote {
        querySchema[WebHookId]("webhook")
          .filter(p => p.id == lift(id))
          .delete
      }
    }.provide(ZLayer.succeed(ds)).map(id => ())
}

object PersistentH2WebHookRepo {
  def layer: ZLayer[Any, Throwable, PersistentH2WebHookRepo] =
    Quill.DataSource.fromPrefix("WebHookApp") >>>
      ZLayer.fromFunction(PersistentH2WebHookRepo(_))
}