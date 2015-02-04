import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "xmlParser"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "mysql" % "mysql-connector-java" % "5.1.18",
    "org.jsoup" % "jsoup" % "1.8.1",
    "org.json" % "json" % "20090211"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
