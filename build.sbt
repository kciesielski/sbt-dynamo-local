import sbt._, Keys._
import SonatypeKeys._
import ScriptedPlugin.{scriptedLaunchOpts,scriptedBufferLog}
import scalariform.formatter.preferences.{SpacesAroundMultiImports, CompactControlReadability, PreserveSpaceBeforeArguments, DoubleIndentClassDeclaration}

name := "sbt-dynamo-local"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

val akkaVersion = "2.3.12"
val akkaStreamVersion = "1.0"
val curatorVersion = "2.9.0"

val dependencies = Seq(
  "com.jcabi" % "DynamoDBLocal" % "2015-07-16",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

val baseSettings =
  sonatypeSettings ++ scalariformSettings ++ Seq(
    organization := "com.softwaremill.build",
    startYear := Some(2015),
    homepage := Some(url("https://github.com/softwaremill/sbt-dynamo-local")),
    scalaVersion := "2.10.5",
    scriptedLaunchOpts ++=
      Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value, "-Dscripted=true"),
    scriptedBufferLog  := false,
    fork               := true,
    sbtPlugin          := true,
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",       // yes, this is 2 args
      "-feature",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture"
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(PreserveSpaceBeforeArguments, true)
      .setPreference(CompactControlReadability, true)
      .setPreference(SpacesAroundMultiImports, false))

val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := {
    x => false
  },
  pomExtra := (
    <scm>
      <url>git@github.com:kciesielski/reactive-kafka.git</url>
      <connection>scm:git:git@github.com:kciesielski/reactive-kafka.git</connection>
    </scm>
      <developers>
        <developer>
          <id>kciesielski</id>
          <name>Krzysztof Ciesielski</name>
          <url>https://twitter.com/kpciesielski</url>
        </developer>
      </developers>
    ))

lazy val `sbt-dynamo-local` =
  project.in( file(".") )
    .settings(baseSettings)
    .settings(publishSettings)
    .settings(Seq(libraryDependencies ++= dependencies))