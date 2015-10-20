package com.softwaremill.build

import java.io.File

import sbt.Keys._
import sbt._
import Classpaths.managedJars

object DynamoLocal {

  var processOpt: Option[Process] = None
  lazy val zipToExtract = TaskKey[File]("zip-to-extract", "ZIP file to be extracted")
  val ZipPath = "cache/com.jcabi/DynamoDBLocal/zips/DynamoDBLocal-2015-07-16.zip"
  val JarName = "DynamoDBLocal.jar"
  val TmpTargetSubDir = "_dynamoLocal"
  val Port = 8000

  def unpackStep(s: Keys.TaskStreams, updateReport: UpdateReport, ivyPaths: IvyPaths, targetDir: File) = Tests.Setup(() => {
    if (!(targetDir / TmpTargetSubDir / JarName).exists()) {
      val pathOpt = ivyPaths
        .ivyHome // TODO use managed resources
        .map(_ / ZipPath)

      val path = pathOpt.getOrElse(failZipNotFound())
      if (!path.exists()) failZipNotFound()
      s.log.info(s"Dynamo Local: unpacking $path")

      val unpackDir = targetDir / TmpTargetSubDir
      IO.unzip(path, unpackDir)
      ()
    }
  })

  def failZipNotFound(): Nothing =
    throw new IllegalStateException(s"Cannot find DynamoDBLocal zip in ivy cache. Please reload SBT. Missing file in ivy home: $ZipPath")

  def stopProcess(): Unit = {
    processOpt.foreach(_.destroy())
    processOpt = None
  }

  def setupStep(s: Keys.TaskStreams, targetDir: File) = Tests.Setup(() => {
    stopProcess()
    s.log.info("Dynamo Local: starting")
    val java = JavaPath.resolve
    val sep = File.separator
    val dist = (targetDir / TmpTargetSubDir).toString
    val cmd = s"$java -Djava.library.path=$dist${sep}DynamoDBLocal_lib -jar DynamoDBLocal.jar --port $Port -inMemory"
    processOpt = Some(Process(cmd, targetDir / TmpTargetSubDir).run())
  })

  def cleanupStep(s: Keys.TaskStreams) = Tests.Cleanup(() => {
    s.log.info("Dynamo Local: stopping")
    try {
      stopProcess()
      s.log.debug("Dynamo Local stopped")
    }
    catch {
      case t: Exception => s.log.warn("Failed to stop Dynamo Local due to: " + t)
    }
  })

  def settings: Seq[Setting[_]] = Seq(
    testOptions in Test += unpackStep(streams.value, update.value, ivyPaths.value, target.value),
    testOptions in Test += setupStep(streams.value, target.value),
    testOptions in Test += cleanupStep(streams.value)
  )

}
