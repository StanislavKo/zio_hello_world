package com.hsd.cv.webhooks.utils

import org.scalatest.flatspec.AnyFlatSpec

class NetworkUtilsSpec extends AnyFlatSpec {

  it should "send request and return content" in {
    assert {
      val response = NetworkUtils.sendHttp("https://beeline.ru/")
      response match {
        case Left(a) => true
        case Right(b) => false
      }
    }
  }
}
