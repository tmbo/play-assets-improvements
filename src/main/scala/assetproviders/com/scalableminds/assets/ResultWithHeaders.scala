package com.scalableminds.assets

import play.api.mvc._

object ResultWithHeaders {
  import play.api.mvc.Result
  type ResultWithHeaders = Result { def withHeaders(headers: (String, String)*): Result }
}