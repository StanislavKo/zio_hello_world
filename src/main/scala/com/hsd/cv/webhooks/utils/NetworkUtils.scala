package com.hsd.cv.webhooks.utils

import java.io.IOException
import java.net.{HttpURLConnection, URL}
import scala.io.Source

object NetworkUtils {

  def sendHttp(urlStr: String): Either[String, Exception] = {
    try {
      val url = new URL(urlStr)
      val con: HttpURLConnection = url.openConnection.asInstanceOf[HttpURLConnection]
      con.setConnectTimeout(2000)
      con.setReadTimeout(2000)
      con.setRequestMethod("GET")
      val inputStream = con.getInputStream
      val content = Source.fromInputStream(inputStream).mkString
      if (inputStream != null) inputStream.close()
      Left(content)
    } catch {
      case e: IOException => {
        Right(e)
      }
    }
  }

}
