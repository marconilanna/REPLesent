/*
 * Copyright 2015 Marconi Lanna
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

version := "0.6"

scalaVersion := "2.11.5"

scalaSource in Test := baseDirectory.value / "test"

resourceDirectory in Test := (scalaSource in Test).value / "resources"

scalacOptions ++= Seq(
    "-deprecation"            // Emit warning and location for usages of deprecated APIs
  , "-encoding", "UTF-8"      // Specify character encoding used by source files
  , "-feature"                // Emit warning and location for usages of features that should be imported explicitly
//  , "-language:_"             // Enable or disable language features (see list below)
//  , "-optimise"               // Generates faster bytecode by applying optimisations to the program
  , "-target:jvm-1.7"         // Target platform for object files
  , "-unchecked"              // Enable additional warnings where generated code depends on assumptions
// Doesn't play well with ScalaTest
//  , "-Xdev"                   // Indicates user is a developer - issue warnings about anything which seems amiss
  , "-Xfatal-warnings"        // Fail the compilation if there are any warnings
  , "-Xlint:_,-nullary-unit"  // Enable or disable specific warnings (see list below)
  , "-Yno-adapted-args"       // Do not adapt an argument list to match the receiver
  , "-Ywarn-dead-code"        // Warn when dead code is identified
// Not really useful
//  , "-Ywarn-numeric-widen"    // Warn when numerics are widened
  , "-Ywarn-unused"           // Warn when local and private vals, vars, defs, and types are are unused
  , "-Ywarn-unused-import"    // Warn when imports are unused
  , "-Ywarn-value-discard"    // Warn when non-Unit expression results are unused
)

/*
scalac -language:help

dynamics             Allow direct or indirect subclasses of scala.Dynamic
existentials         Existential types (besides wildcard types) can be written and inferred
experimental.macros  Allow macro defintion (besides implementation and application)
higherKinds          Allow higher-kinded types
implicitConversions  Allow definition of implicit functions called views
postfixOps           Allow postfix operator notation, such as `1 to 10 toList'
reflectiveCalls      Allow reflective access to members of structural types
*/

/*
scalac -Xlint:help

adapted-args               Warn if an argument list is modified to match the receiver
by-name-right-associative  By-name parameter of right associative operator
delayedinit-select         Selecting member of DelayedInit
doc-detached               A ScalaDoc comment appears to be detached from its element
inaccessible               Warn about inaccessible types in method signatures
infer-any                  Warn when a type argument is inferred to be `Any`
missing-interpolator       A string literal appears to be missing an interpolator id
nullary-override           Warn when non-nullary `def f()' overrides nullary `def f'
nullary-unit               Warn when nullary methods return Unit
option-implicit            Option.apply used implicit view
package-object-classes     Class or object defined in package object
poly-implicit-overload     Parameterized overloaded implicit methods are not visible as view bounds
private-shadow             A private field (or class parameter) shadows a superclass field
type-parameter-shadow      A local type parameter shadows a type already in scope
unsound-match              Pattern match may not be typesafe
*/

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

// Improved incremental compilation
incOptions := incOptions.value.withNameHashing(true)

// Improved dependency management
updateOptions := updateOptions.value.withCachedResolution(true)

// Download and create Eclipse source attachments for library dependencies
// EclipseKeys.withSource := true

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
