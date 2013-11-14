package com.scalableminds.assets

import ResultWithHeaders.ResultWithHeaders

import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.DateTimeZone

import play.api.Play.current
import play.api.mvc.Controller
import play.api.mvc.Result
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Call
import play.api.{Logger, Play}

/**
 * Pipelines CDN access for static files. Mix this trait in and provide remoteContentUrl to
 * have all calls from your views to your assets automatically resolve to a url with the
 * correct domain.
 *
 * Inspired by http://www.jamesward.com/2012/08/08/edge-caching-with-play2-heroku-cloudfront
 */
trait RemoteAssets extends AssetProvider { this: Controller =>
  /**
   * This application's content URL with protocol. For example, "https://souefusfisu.cloudfront.net"
   */
  protected def remoteContentUrl: Option[String]

  private val timeZoneCode = "GMT"

  private val df: DateTimeFormatter =
    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss '" + timeZoneCode + "'").withLocale(java.util.Locale.ENGLISH).withZone(DateTimeZone.forID(timeZoneCode))

  abstract override def at(asset: PiplineAsset): Action[AnyContent] = Action { implicit request =>
    val action = super.at(asset)
    val result = action.apply(request)
    val resultWithHeaders = result.asInstanceOf[ResultWithHeaders]
    resultWithHeaders.withHeaders(
      DATE -> df.print({ new java.util.Date }.getTime),
      "Access-Control-Allow-Origin" -> "*")
  }

  abstract override def pathFor(asset: PiplineAsset): String = {
    remoteContentUrl match {
      case Some(contentUrl) =>
        Logger.trace("Unbinding: " + super.pathFor(asset))
        contentUrl + super.pathFor(asset)
      case None =>
        super.pathFor(asset)
    }
  }
}
