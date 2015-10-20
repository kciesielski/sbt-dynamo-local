package com.softwaremill.build

import sbt.Keys._
import sbt._
import Classpaths.managedJars

object DynamoLocal {

  lazy val zipToExtract = TaskKey[File]("zip-to-extract", "ZIP file to be extracted")
  val ZipPath = "cache/com.jcabi/DynamoDBLocal/zips/DynamoDBLocal-2015-07-16.zip"

  def unpackStep(s: Keys.TaskStreams, updateReport: UpdateReport, ivyPaths: IvyPaths, targetDir: File) = Tests.Setup(() => {
    val pathOpt = ivyPaths
      .ivyHome // TODO use managed resources
      .map(_ / ZipPath)

    val path = pathOpt.getOrElse(failZipNotFound())
    if (!path.exists()) failZipNotFound()
    s.log.info(s"Dynamo Local: unpacking $path")

    val unpackDir = targetDir / "_dynamoLocal"
    IO.unzip(path, unpackDir)
    ()
  })

  def failZipNotFound(): Nothing =
    throw new IllegalStateException(s"Cannot find DynamoDBLocal zip in ivy cache. Please reload SBT. Missing file in ivy home: $ZipPath")

  def setupStep(s: Keys.TaskStreams, baseDir: File) = Tests.Setup(() => {
    s.log.info("Dynamo Local: starting")
  })

  def cleanupStep(s: Keys.TaskStreams) = Tests.Cleanup(() => {
    s.log.debug("Dynamo Local: stopping")
    try {
      s.log.debug("Dynamo Local stopped")
    }
    catch {
      case t: Exception => s.log.warn("Failed to stop Dynamo Local due to: " + t)
    }
  })

  def settings: Seq[Setting[_]] = Seq(
    testOptions in Test += unpackStep(streams.value, update.value, ivyPaths.value, target.value),
    testOptions in Test += setupStep(streams.value, baseDirectory.value),
    testOptions in Test += cleanupStep(streams.value)
  )

}
