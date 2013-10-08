import sbt._
import Keys._
import play.Project._

object BuildSettings {
  val buildVersion = scala.io.Source.fromFile("version").mkString.trim

  val filter = { (ms: Seq[(File, String)]) =>
    ms filter {
      case (file, path) =>
        path != "logback.xml" && !path.startsWith("toignore") && !path.startsWith("samples")
    }
  }

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.scalableminds",
    version := buildVersion,
    scalaVersion := "2.10.2",
    javaOptions in test ++= Seq("-Xmx512m", "-XX:MaxPermSize=512m"),
    scalacOptions ++= Seq("-unchecked", "-deprecation" /*, "-Xlog-implicits", "-Yinfer-debug", "-Xprint:typer", "-Yinfer-debug", "-Xlog-implicits", "-Xprint:typer"*/ ),
    scalacOptions in (Compile, doc) ++= Seq("-unchecked", "-deprecation", "-diagrams", "-implicits"),
    shellPrompt := ShellPrompt.buildShellPrompt,
    mappings in (Compile, packageBin) ~= filter,
    mappings in (Compile, packageSrc) ~= filter,
    mappings in (Compile, packageDoc) ~= filter) ++ Publish.settings // ++ Format.settings
}

object Publish {
  object TargetRepository {
    def scmio: Project.Initialize[Option[sbt.Resolver]] = version { (version: String) =>
      val rootDir = "/srv/maven/"
      val path =
        if (version.trim.endsWith("SNAPSHOT")) 
          rootDir + "snapshots/" 
        else 
          rootDir + "releases/" 
      Some(Resolver.sftp("scm.io intern repo", "scm.io", 44144, path))
    }
  }
  lazy val settings = Seq(
    publishMavenStyle := true,
    publishTo <<= TargetRepository.scmio,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    homepage := Some(url("http://scm.io")))
}

object Colors {

  import scala.Console._

  lazy val isANSISupported = {
    Option(System.getProperty("sbt.log.noformat")).map(_ != "true").orElse {
      Option(System.getProperty("os.name"))
        .map(_.toLowerCase)
        .filter(_.contains("windows"))
        .map(_ => false)
    }.getOrElse(true)
  }

  def red(str: String): String = if (isANSISupported) (RED + str + RESET) else str
  def blue(str: String): String = if (isANSISupported) (BLUE + str + RESET) else str
  def cyan(str: String): String = if (isANSISupported) (CYAN + str + RESET) else str
  def green(str: String): String = if (isANSISupported) (GREEN + str + RESET) else str
  def magenta(str: String): String = if (isANSISupported) (MAGENTA + str + RESET) else str
  def white(str: String): String = if (isANSISupported) (WHITE + str + RESET) else str
  def black(str: String): String = if (isANSISupported) (BLACK + str + RESET) else str
  def yellow(str: String): String = if (isANSISupported) (YELLOW + str + RESET) else str

}

// Shell prompt which show the current project,
// git branch and build version
object ShellPrompt {
  object devnull extends ProcessLogger {
    def info(s: => String) {}

    def error(s: => String) {}

    def buffer[T](f: => T): T = f
  }

  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
    getOrElse "-" stripPrefix "## ")

  val buildShellPrompt = {
    (state: State) =>
      {
        val currProject = Project.extract(state).currentProject.id
        ("%s "+ Colors.green("(%s)") + ": %s> ").format(
          currProject, currBranch, BuildSettings.buildVersion)
      }
  }
}

object Resolvers {
  val resolversList = Seq(

    )
}

object Dependencies {

}

object BraingamesLibraries extends Build {
  import BuildSettings._
  import Resolvers._
  import Dependencies._

  lazy val util = Project(
    "play-assets-improvements",
    file("."),
    settings = buildSettings ++ Seq(
      resolvers := resolversList,
      libraryDependencies ++= Seq()))
}