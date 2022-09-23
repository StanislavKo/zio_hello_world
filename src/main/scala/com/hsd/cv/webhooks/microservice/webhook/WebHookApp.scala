package com.hsd.cv.webhooks.microservice.webhook

import com.hsd.cv.webhooks.microservice.webhook.model.{WebHook, WebHookId}
import com.hsd.cv.webhooks.microservice.webhook.repository.WebHookRepo
import com.hsd.cv.webhooks.microservice.webhook.validator.WebHookValidatorService
import zhttp.http.*
import zio.*
import zio.ZIO.{none, suspendSucceed}
import zio.json.*

object WebHookApp {
  def apply(): Http[WebHookRepo with WebHookValidatorService with Unit, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      // POST /webhooks
      case req@(Method.POST -> !! / "webhooks") =>
        for {
          u <- req.bodyAsString.map(_.fromJson[WebHook])
          r <- u match
            case Left(e) =>
              ZIO
                .debug(s"Failed to parse the input: $e")
                .as(Response.text(e).setStatus(Status.BadRequest))
            case Right(u) => {
              for {
                validationUrl <- ZIO.service[WebHookValidatorService].flatMap(!_.validateUrl(u))
                validationParams <- ZIO.service[WebHookValidatorService].flatMap(!_.validateParams(u))
                response2 <- suspendSucceed {
                  if (validationUrl && validationParams)
                    WebHookRepo.register(u).map(id => Response.text(id.toString))
                  else
                    ZIO
                      .debug(s"Validation is failed: validationUrl=$validationUrl, validationParams=$validationParams")
                      .as(Response.text("Validation is failed"))
                }
              } yield response2
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