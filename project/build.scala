import sbt._
import Defaults._
import Keys._

object ApplicationBuild extends Build {

  lazy val commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.chrisstucchio",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalaVersion := "2.12.2",
    crossScalaVersions := Seq("2.11.6", "2.12.0"),
    version := "0.19.4",
    resolvers ++= myResolvers,
    name := "tiramisu",
    //fork := true,
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.2.13",
      "joda-time" % "joda-time" % "2.9.9",
      "org.joda" % "joda-convert" % "1.8",
      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
      "org.postgresql" % "postgresql" % "42.1.1"
    ),
    publishTo := Some(Resolver.file("file",  new File( "/tmp/tiramisu-publish" )) )
  )

  val myResolvers = Seq("Sonatatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
			"Sonatatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
			"Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
			"Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots",
			"Coda Hale" at "http://repo.codahale.com"
		      )

  lazy val tiramisu = Project("tiramisu", file("."), settings = commonSettings)
}
