package com.hsd.cv.webhooks

import com.hsd.cv.webhooks.utils.NetworkUtils
import org.scalatest.flatspec.AnyFlatSpec

import java.io.IOException
import java.net.{HttpURLConnection, URL}
import scala.io.Source

class HttpApiSpec extends AnyFlatSpec {

  "http api" should "return webhooks" in {
    new Thread(() => {
      MainApp.startHttp()
    }).start()

    Thread.sleep(10_000)

    assert {
      val response = NetworkUtils.sendHttp("http://localhost:28087/webhooks")
      response match {
        case Left(a) => true
        case Right(b) => false
      }
    }
    assert {
      val response = NetworkUtils.sendHttp("http://localhost:28087/webhooks2")
      response match {
        case Left(a) => false
        case Right(b) => true
      }
    }
  }
}
