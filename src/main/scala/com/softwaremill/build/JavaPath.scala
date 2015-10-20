package com.softwaremill.build

import java.io.File

object JavaPath {
  def resolve = sys.env.get("JAVA_HOME")
    .map(javaHome => s"${javaHome.stripSuffix(File.separator)}")
    .map(Seq(_, "bin", "").mkString(File.separator))
    .getOrElse("") + "java"
}
