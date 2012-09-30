import sbt._
import Defaults._
import Keys._
import com.github.retronym.SbtOneJar

object ApplicationBuild extends Build {

  lazy val commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.chrisstucchio",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalaVersion := "2.9.1",
    version := "0.1",
    resolvers ++= myResolvers,
    name := "tiramisu",
    mainClass := Some("com.patch.injera.BeaconServer"),
    //fork := true,
    libraryDependencies ++= Seq("org.scalaz" %% "scalaz-core" % "7.0-SNAPSHOT",
				"play" %% "anorm" % "2.0.4",
				"org.scalacheck" %% "scalacheck" % "1.10.0" % "test"
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
