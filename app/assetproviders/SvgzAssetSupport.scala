package assetproviders

import assetproviders.ResultWithHeaders.ResultWithHeaders
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.AnyContent

/**
 * Gives asset support for .svgz files, which require the following headers to be processed
 * properly:
 * {{{
 * Content-Type: "image/svg+xml"
 * Content-Encoding: gzip
 * }}}
 *
 * See [[http://kaioa.com/node/45 this link for more info]].
 */
trait SvgzAssetSupport extends AssetProvider { this: Controller =>
  abstract override def at(asset: PiplineAsset): Action[AnyContent] = {
    if (!asset.file.endsWith(".svgz")) {
      super.at(asset)
    } else {
      Action { request =>
        val result = super.at(asset).apply(request).asInstanceOf[ResultWithHeaders]

        result.withHeaders(
          "Content-Encoding" -> "gzip",
          "Content-Type" -> "image/svg+xml"
        )
      }
    }
  }
}
