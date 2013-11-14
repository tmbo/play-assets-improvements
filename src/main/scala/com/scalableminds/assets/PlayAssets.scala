package com.scalableminds.assets

import play.api.mvc._

/**
 * An AssetProvider view of Play's built in Asset controller that can be mixed
 * into other AssetProviders.
 */
trait PlayAssets extends AssetProvider { this: Controller =>
  //override def at(file: String): Call = assetReverseRoute(file)

  def at(asset: PiplineAsset): Action[AnyContent] = controllers.Assets.at(asset.path, asset.file)

  def bind(file: String): PiplineAsset = PiplineAsset(file, defaultPath)

  def pathFor(asset: PiplineAsset) = assetReverseRoute(asset).url
  
  /**
   * This is the method that will be called by the templates mostly to get
   * a Call that enables them to get the external URL of the asset.
   */
  def unbind(asset: PiplineAsset): String = asset.file
}