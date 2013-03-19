package controllers

import play.api.Play.current
import play.api.mvc.Controller
import assetproviders._

/** Collects all asset transformations into a single trait for use in our website app */
trait MyAssetPipeline
  extends PlayAssets
     with RemoteAssets
     with FingerprintedAssets
     with SvgzAssetSupport{ this: Controller => }

/** The concrete asset implementation for our website app */
object MyAssets extends Controller with MyAssetPipeline {
  override def assetReverseRoute(file: String) = controllers.routes.MyAssets.at(file)

  override val defaultPath = "/public"

  override val remoteContentUrl = {
    val cfg = current.configuration

    cfg.getString("cdn.contenturl").map { contentUrl =>
      val secure = cfg.getBoolean("cdn.secure").getOrElse(true)
      val protocol = if (secure) "https" else "http"

      protocol + "://" + contentUrl
    }
  }

  override val cacheControlMaxAgeInSeconds = {
    val cfg = current.configuration

    cfg.getInt("assets.immutable.cacheControlInSeconds").getOrElse(31536000) // default 1 metric year
  }
}
