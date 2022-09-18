package com.hsd.cv.webhooks

import zio.test._
import com.hsd.cv.webhooks.MainApp._

object MainAppSpec extends ZIOSpecDefault {

  def spec = suite("MainAppSpec")(
    test("noTest") {
      assertTrue(true)
    }
  )

}
