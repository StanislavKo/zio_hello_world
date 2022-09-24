package com.hsd.cv.webhooks.microservice.webhook.service

import com.hsd.cv.webhooks.microservice.webhook.model.WebHook
import com.hsd.cv.webhooks.microservice.webhook.repository.WebHookRepo
import com.hsd.cv.webhooks.microservice.webhook.validator.WebHookValidatorService
import zhttp.http._
import zio.json.{DecoderOps, EncoderOps}
import zio.{ZIO, ZLayer}

object WebHookApp {
  def apply(): Http[WebHookRepo with WebHookValidatorService with Unit, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      // POST /webhooks
      case req@(Method.POST -> !! / "webhooks") =>
        for {
          u <- req.bodyAsString.map(_.fromJson[WebHook])
          r <- u match {
            case Left(e) =>
              ZIO
                .debug(s"Failed to parse the input: $e")
                .as(Response.text(e).setStatus(Status.BadRequest))
            case Right(u) => {
              for {
                isUrlValid <- ZIO.service[WebHookValidatorService].flatMap(_.isUrlValid(u))
                isParamsValid <- ZIO.service[WebHookValidatorService].flatMap(_.isParamsValid(u))
                response2 <- ZIO.suspendSucceed {
                  if (isUrlValid && isParamsValid)
                    WebHookRepo.register(u).map(id => Response.text(id.toString))
                  else
                    ZIO
                      .debug(s"Validation is failed: isUrlValid=$isUrlValid, isParamsValid=$isParamsValid")
                      .as(Response.text("Validation is failed"))
                }
              } yield response2
            }
          }
        } yield r

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

  val layerUnit: ZLayer[WebHookRepo, Throwable, Unit] =
    ZLayer.fromFunction(() => ())

}
