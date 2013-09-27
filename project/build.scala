import sbt._
import Defaults._
import Keys._

object ApplicationBuild extends Build {

  lazy val commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.chrisstucchio",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalaVersion := "2.10.0",
    version := "0.1",
    resolvers ++= myResolvers,
    name := "tiramisu",
    //fork := true,
    libraryDependencies ++= Seq("org.scalaz" %% "scalaz-core" % "7.0-SNAPSHOT",
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
      "joda-time" % "joda-time" % "2.0",
      "org.joda" % "joda-convert" % "1.1"
    )

  )

  val myResolvers = Seq("Sonatatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
			"Sonatatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
			"Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
			"Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots",
			"Coda Hale" at "http://repo.codahale.com"
		      )

  lazy val injera = Project("tiramisu", file("."), settings = commonSettings)

}
