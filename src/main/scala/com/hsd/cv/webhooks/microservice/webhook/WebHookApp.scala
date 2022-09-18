package com.hsd.cv.webhooks.microservice.webhook

import com.hsd.cv.webhooks.microservice.webhook.model.WebHook
import com.hsd.cv.webhooks.microservice.webhook.repository.WebHookRepo
import zhttp.http.*
import zio.*
import zio.json.*

object WebHookApp:
  def apply(): Http[WebHookRepo, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      // POST /webhooks
      case req@(Method.POST -> !! / "webhooks") =>
        for
          u <- req.bodyAsString.map(_.fromJson[WebHook])
          r <- u match
            case Left(e) =>
              ZIO.debug(s"Failed to parse the input: $e").as(
                Response.text(e).setStatus(Status.BadRequest)
              )
            case Right(u) =>
              WebHookRepo.register(u)
                .map(id => Response.text(id.toString))
        yield r

      // GET /webhooks/:id
      case Method.GET -> !! / "webhooks" / id =>
        WebHookRepo.lookup(id.toLong)
          .map {
            case Some(webhook) =>
              Response.json(webhook.toJson)
            case None =>
              Response.status(Status.NotFound)
          }

      // GET /webhooks
      case Method.GET -> !! / "webhooks" =>
        WebHookRepo.webhooks.map(response => Response.json(response.toJson))

      // DELETE /webhooks/:id
      case Method.DELETE -> !! / "webhooks" / id => {
        WebHookRepo.delete(id.toLong).map(_ => Response.status(Status.Ok))
      }

    }

