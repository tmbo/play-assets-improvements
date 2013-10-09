package assetproviders

import assetproviders.ResultWithHeaders.ResultWithHeaders
import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import play.api.Play.current
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.AnyContent
import play.api.mvc.Call
import play.api.Play
import play.api.Logger
import java.io.InputStream
import play.api.mvc.PathBindable
import java.util.zip.{CRC32, Checksum, CheckedInputStream}
import com.google.common.io.{InputSupplier, ByteStreams, Files}
import com.google.common.hash.Hashing

/**
 * Pipelines fingerprinting for your static assets, which allows you to improve site
 * performance by setting very long cache expiries. Assets are fingerprinted like this:
 *   original      = foo.jpg
 *   fingerprinted = foo-fp-1231343451234.jpg
 *
 * Where '-fp-1231343451234' is the 'fingerprint' and '1231343451234' is a checksum of
 * the file contents.
 *
 * We got some inspiration from ruby on rails, but the solution was a bit obvious.
 * http://guides.rubyonrails.org/asset_pipeline.html#what-is-fingerprinting-and-why-should-i-care
 */

trait FingerprintedAssets extends AssetProvider { this: Controller =>
  import java.net.URL

  private val fileToFingerprinted = new ConcurrentHashMap[String, String]()

  private val fingerprintConstant = "-v"

  val cacheControlMaxAgeInSeconds: Int

  lazy val cacheControlMaxAge = "max-age=" + cacheControlMaxAgeInSeconds

  /**
   * This will find files that were fingerprinted.  If the file is found with the fingerprint
   * striped out of its name and the checksum matches the checksum in the fingerprint.  Otherwise
   * it will just return whatever the super.at(path, file) returns.
   */
  abstract override def bind(file: String): PiplineAsset = {
    val (baseFilename, extension) = splitFilename(file)
    if (!baseFilename.contains(fingerprintConstant)) { // this may not have been fingerprinted, we should fall back to without fingerprinting
      super.bind(file)
    } else {
      val (originalBaseFilename, fingerprint) = file.splitAt(file.lastIndexOf(fingerprintConstant))
      val originalFilename = originalBaseFilename + "." + extension

      val originalOrFingerprinted = originalOrFingerprint(PiplineAsset(originalFilename, defaultPath))

      originalOrFingerprinted.fold(
        originalFilename => {
          Logger.info("Could not find asset at " + originalFilename + " for file =" + file)
          super.bind(file) // maybe another asset provider knows what to do with this 
        },
        fingerprintedFilename => {
          if (fingerprintedFilename != file) {
            Logger.info("expected checksum = " + file + " but we a file with a different checksum = " + fingerprintedFilename)
            super.bind(file) // again, this asset provider doesn't have this asset
          } else {
            super.bind(originalFilename)
          }
        })
    }
  }

  abstract override def unbind(asset: PiplineAsset): String = {
    val originalOrFingerprinted = originalOrFingerprint(asset)

    originalOrFingerprinted.fold({
      originalFilename => super.unbind(asset.copy(file = originalFilename))
    }, {
      fingerprintedFilename => super.unbind(asset.copy(file = fingerprintedFilename))
    })
  }

  /**
   * If there the original file exists and can be fingerprinted or has been fingerprinted the
   * fingerprinted filename is on the Right, otherwise the originalFilename is on the Left.
   */
  private def originalOrFingerprint(asset: PiplineAsset): Either[String, String] = {
    if (fileToFingerprinted.contains(asset.resourceName)) {
      Right(fileToFingerprinted.get(asset.resourceName))
    } else {
      defaultUrl(asset.resourceName) match {
        case None =>
          Left(asset.file)
        case Some(url) =>
          val fingerprintedFilename = fingerprintFile(asset, url)
          Right(fingerprintedFilename)
      }
    }
  }

  private def defaultUrl(file: String) = {
    Play.resource(file)
  }

  // Updates the fileToFingerprinted map with the fingerprinted file
  private def fingerprintFile(asset: PiplineAsset, url: URL): String = {
    val checksum = getChecksum(url)
    val (baseFilename, extension) = splitFilename(asset.file)
    val fingerprintedFilename = 
      baseFilename + fingerprintConstant + checksum + "." + extension

    fileToFingerprinted.put(asset.resourceName, fingerprintedFilename)
    fingerprintedFilename
  }

  private def getChecksum(url: URL): Long = {
    //val assetStream = url.openStream()
    val is = new InputSupplier[InputStream]{
      def getInput() : InputStream =
        url.openStream()
    }

    ByteStreams.hash(is, Hashing.crc32).padToLong()
  }

  private def splitFilename(filename: String): (String, String) = {
    val extension = getFileExtension(filename)
    val uncleanBaseFilename = removeAfterLastOccurrence(filename, extension)
    val baseFilename = if (uncleanBaseFilename.endsWith("."))
      uncleanBaseFilename.dropRight(1)
    else uncleanBaseFilename

    (baseFilename, extension)
  }

  // returns everything after last occurrence of the other string, requires
  //first arg must contain second arg
  private def removeAfterLastOccurrence(str: String, toRemove: String) = {
    str.take(str.lastIndexOf(toRemove))
  }

  /**
   * Returns the <a href="http://en.wikipedia.org/wiki/Filename_extension">file
   * extension</a> for the given file name, or the empty string if the file has
   * no extension.  The result does not include the '{@code .}'.
   *
   * Adopted from Google Guava r11 since Play 2.0 only gives us Guava r10
   */
  private def getFileExtension(fullName: String): String = {
    val fileName = new File(fullName).getName()
    val dotIndex = fileName.lastIndexOf('.')
    if (dotIndex == -1)
      ""
    else fileName.substring(dotIndex + 1)
  }
}
