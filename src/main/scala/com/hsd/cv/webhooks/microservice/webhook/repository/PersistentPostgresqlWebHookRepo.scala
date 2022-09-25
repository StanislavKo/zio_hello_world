package com.hsd.cv.webhooks.microservice.webhook.repository

import com.hsd.cv.webhooks.microservice.webhook.model.{WebHook, WebHookId}
import io.getquill._
import io.getquill.jdbczio.Quill
import zio._

import java.sql.{Connection, DatabaseMetaData, Statement}
import javax.sql.DataSource
import scala.collection.mutable.ListBuffer

case class PersistentPostgresqlWebHookRepo(ds: DataSource) extends WebHookRepo {
  val ctx                          = new PostgresZioJdbcContext(Escape)
  val TRANSACTION_READ_UNCOMMITTED = 1

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
                webHook.volume,
                webHook.description,
                webHook.desccode
              )
            )
          }
          .returningGenerated(_.id)
      }
    }
    id
  }.provide(ZLayer.succeed(ds))

//  override def registerSlow(webHook: WebHook): Task[Long] = {
//    val id = ctx.transaction {
//      val id = ctx.run {
//        quote {
//          querySchema[WebHookId]("webhook")
//            .insertValue {
//              lift(
//                WebHookId(
//                  0,
//                  webHook.url,
//                  webHook.topic,
//                  webHook.format,
//                  webHook.volume,
//                  webHook.description,
//                  webHook.desccode
//                )
//              )
//            }
//            .returningGenerated(_.id)
//        }
//      }
//      Thread.sleep(10_000)
//      id
//    }
//    id
//  }.provide(ZLayer.succeed(ds))

  override def registerSlow(webHook: WebHook): Task[Long] = {
    ctx
      .transaction {
        ZIO
          .environment[DataSource]
          .flatMap(env =>
            for {
              id1 <- {
                val con: Connection = env.get(Tag[Environment]).getConnection
                con.setAutoCommit(false)
                val dbmd: DatabaseMetaData = con.getMetaData
                println(
                  s"DatabaseMetaData.isolations_1=${dbmd.supportsTransactionIsolationLevel(1)}"
                )
                con.setTransactionIsolation(1)
                val stmt = con.prepareStatement(
                  "insert into webhook(url,topic,format,volume,description,desccode) values (?,?,?,?,?,?)"
                  , Statement.RETURN_GENERATED_KEYS
                )
                stmt.setString(1, webHook.url)
                stmt.setString(2, webHook.topic)
                stmt.setString(3, webHook.format)
                stmt.setString(4, webHook.volume)
                stmt.setString(5, webHook.description)
                stmt.setString(6, webHook.desccode)
                stmt.executeUpdate()
                val id = try {
                  val generatedKeys = stmt.getGeneratedKeys
                  try if (generatedKeys.next) generatedKeys.getLong(1)
                  else 0
                  finally if (generatedKeys != null) generatedKeys.close()
                }

                Thread.sleep(10_000)

                con.commit()

                ZIO.succeed(id)
              }
            } yield id1
          )
          .provide(ZLayer.succeed(ds))
      }.provide(ZLayer.succeed(ds))
  }

  override def lookup(id: Long): Task[Option[WebHookId]] =
    ctx
      .run {
        quote {
          querySchema[WebHookId]("webhook")
            .filter(p => p.id == lift(id))
            .map(wh =>
              WebHookId(
                wh.id,
                wh.url,
                wh.topic,
                wh.format,
                wh.volume,
                wh.description,
                wh.desccode
              )
            )
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
            .map(wh =>
              WebHookId(
                wh.id,
                wh.url,
                wh.topic,
                wh.format,
                wh.volume,
                wh.description,
                wh.desccode
              )
            )
        }
      }
      .provide(ZLayer.succeed(ds))
      .map(_.headOption)

  override def webhooks: Task[List[WebHookId]] =
    ctx
      .run {
        quote {
          querySchema[WebHookId]("webhook").map(wh =>
            WebHookId(
              wh.id,
              wh.url,
              wh.topic,
              wh.format,
              wh.volume,
              wh.description,
              wh.desccode
            )
          )
        }
      }
      .provide(ZLayer.succeed(ds))

  override def webhooksUncommitted: Task[List[WebHookId]] = {
    ZIO
      .environment[DataSource]
      .flatMap(env =>
        for {
          list <- {
            val con: Connection        = env.get(Tag[Environment]).getConnection
            val dbmd: DatabaseMetaData = con.getMetaData
            println(
              s"DatabaseMetaData.isolations_1=${dbmd.supportsTransactionIsolationLevel(1)}"
            )
            con.setTransactionIsolation(TRANSACTION_READ_UNCOMMITTED)
            val stmt     = con.createStatement()
            val rs       = stmt.executeQuery("select * from webhook")
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
            ZIO.succeed(webhooks.toList)
          }
        } yield list
      )
      .provide(ZLayer.succeed(ds))
  }

//  override def webhooksUncommitted: Task[List[WebHookId]] = {
//    readUncommited {
//      ctx
//        .run {
//          quote {
//            querySchema[WebHookId]("webhook").map(wh =>
//              WebHookId(wh.id, wh.url, wh.topic, wh.format, wh.volume, wh.description, wh.desccode)
//            )
//          }
//        }
//        .provide(ZLayer.succeed(ds))
//    }.provide(ZLayer.succeed(ds))
//  }
//
//  def readUncommited(f: ZIO[Any, java.sql.SQLException, List[WebHookId]]) = {
//    val TRANSACTION_READ_UNCOMMITTED = 1
//    ZIO.environment[DataSource].flatMap(env => {
//      for {
//        a <- {
//          val con: Connection = env.get(Tag[Environment]).getConnection
//          val dbmd: DatabaseMetaData = con.getMetaData
//          con.setTransactionIsolation(TRANSACTION_READ_UNCOMMITTED)
//          val stmt = con.createStatement()
//          f.provideEnvironment(env)
//        }
//      } yield a
//    })
//  }

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
