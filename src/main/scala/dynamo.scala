package com.softwaremill.build

import sbt._
import sbt.Keys._

object DynamoLocal {

  private def setupStep(s: Keys.TaskStreams) = Tests.Setup(() => {
    s.log.info("Dynamo Local: starting")
  })

  private def cleanupStep(s: Keys.TaskStreams) = Tests.Cleanup(() => {
    s.log.debug("Dynamo Local: stopping")
    try {
      s.log.debug("Dynamo Local stopped")
    }
    catch {
      case t: Exception => s.log.warn("Failed to stop Dynamo Local due to: " + t)
    }
  })

  def settings: Seq[Setting[_]] = Seq(
    testOptions in Test += setupStep(streams.value),
    testOptions in Test += cleanupStep(streams.value)
  )

}
