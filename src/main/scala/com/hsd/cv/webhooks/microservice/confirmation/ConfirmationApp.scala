package com.hsd.cv.webhooks.microservice.confirmation

import zhttp.http._

object ConfirmationApp {
  def apply(): Http[Any, Nothing, Request, Response] =
    Http.collect[Request] {
      // GET /confirmation?k=:k&v=:v
      case req@(Method.GET -> !! / "confirmation") if (req.url.queryParams.nonEmpty) => {
        println(s"HTTP confirmation k=${req.url.queryParams("k").mkString("; ")}, v=${req.url.queryParams("v").mkString("; ")}")
        Response.text("OK")
      }
    }
}