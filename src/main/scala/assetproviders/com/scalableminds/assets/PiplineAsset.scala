package com.scalableminds.assets

case class PiplineAsset(file: String, path: String) {
  val resourceName = Option(path + "/" + file).map(name => if (name.startsWith("/")) name else ("/" + name)).get
}