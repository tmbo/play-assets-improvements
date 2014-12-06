## Assets improvements plugin for Play 2.3

based on the assets improvements plugin of myyk.

With this 'module' you can get CDNs working easy. It fingerprints assets and allowes remote deployment to CDNs of the
assets.

## Installation

The package is published to sonatype and should get synced to maven central.

You need to add the package as a dependency:

```
libraryDependencies += "com.scalableminds" %% "play-assets-improvements" % "2.3.0"
```


## Configuration

1. Add the dependency on this module.

2. Add a value to your conf/application.conf file that points to your cdn.

```
cdn.contenturl="XXXXXXXXXXXXXX.cloudfront.net
```

By default the cdn assets will use HTTPS.  If you don't want to use HTTPS, add this setting cdn.secure=false.

3. Replace your Assets route in your conf/routes file like this.

If you had:

```
GET     /assets/*file               controllers.Assets.at(path="/public", file)
```

You now need

```
GET     /assets/*file               controllers.MyAssets.at(path=controllers.MyAssets.defaultPath, file)
```

4. Replace all "routes.Assets.at" with "RemoteAssets.at" in your project.  (note, you no longer need the "routes." because you are no longer using the reverse router.)

This content is released under the (Link Goes Here) MIT License.
