/*
 * Copyright 2015-2017 Marconi Lanna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
name := "REPLesent"

version := "1.2"

scalaVersion := "2.12.3"

scalaSource in Test := baseDirectory.value / "test"

resourceDirectory in Test := (scalaSource in Test).value / "resources"

scalacOptions ++= Seq(
    "-deprecation"            // Emit warning and location for usages of deprecated APIs
  , "-encoding", "UTF-8"      // Specify character encoding used by source files
  , "-feature"                // Emit warning and location for usages of features that should be imported explicitly
  , "-target:jvm-1.8"         // Target platform for object files
  , "-unchecked"              // Enable additional warnings where generated code depends on assumptions
  , "-Xfatal-warnings"        // Fail the compilation if there are any warnings
  , "-Xlint:_,-nullary-unit"  // Enable or disable specific warnings (see list below)
  , "-Yno-adapted-args"       // Do not adapt an argument list to match the receiver
  , "-Ywarn-dead-code"        // Warn when dead code is identified
  , "-Ywarn-unused"           // Warn when local and private vals, vars, defs, and types are are unused
  , "-Ywarn-unused-import"    // Warn when imports are unused
  , "-Ywarn-value-discard"    // Warn when non-Unit expression results are unused
)

libraryDependencies ++= Seq(
    "org.scala-lang"  % "scala-compiler" % "2.12.3" % Compile
  , "org.scalatest"  %% "scalatest"      % "3.0.4"  % Test
)

// Improved dependency management
updateOptions := updateOptions.value.withCachedResolution(true)

// Uncomment to enable offline mode
// offline := true

showSuccess := true

showTiming := true

shellPrompt := { state =>
  import scala.Console.{CYAN,RESET}
  val p = Project.extract(state)
  val name = p.getOpt(sbt.Keys.name) getOrElse p.currentProject.id
  s"[$CYAN$name$RESET] $$ "
}
