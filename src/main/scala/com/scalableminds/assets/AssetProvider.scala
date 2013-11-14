package com.scalableminds.assets

import play.api.mvc._

/**
 * This simple interface is meant to mimic the existing interface in Play 2.0
 * for the Assets controller it provides.  By implementing this it is possible
 * to mix and combine various AssetProviders to add additional functionality.
 */
trait AssetProvider { this: Controller =>
  /**
   * This is to be implemented by the concrete class and is supposed to be a
   * call to the reverse router for the at(path, file) call.
   */
  protected def assetReverseRoute(asset: PiplineAsset): Call

  /**
   * This is the method that will be called by the router to serve the
   * asset to the client.
   */
  def at(asset: PiplineAsset): Action[AnyContent]
      
  def bind(file: String): PiplineAsset
  
  /**
   * This is the method that will be called by the templates mostly to get
   * a Call that enables them to get the external URL of the asset.
   */
  def unbind(asset: PiplineAsset): String
  
  
  def pathFor(file: String): String = pathFor(PiplineAsset(file, defaultPath))

  def pathFor(asset: PiplineAsset): String
  
  def defaultPath: String
}
