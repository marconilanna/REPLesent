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
import org.scalatest.FreeSpec

class REPLesentSpec extends FreeSpec {
  def testcase(file: String) = getClass.getResource(s"/test_$file.txt").getPath

  def captureOutput[T](f: => T): (T, String) = {
    val output = new java.io.ByteArrayOutputStream

    val result = Console.withOut(output) {
      f
    }

    (result, output.toString)
  }

  def captureError[T](f: => T): (T, String) = {
    val output = new java.io.ByteArrayOutputStream

    val result = Console.withErr(output) {
      f
    }

    (result, output.toString)
  }

  val empty5x4 =
    """*****
      |*   *
      |*****""".stripMargin

  val empty5x5 =
    """*****
      |*   *
      |*   *
      |*****""".stripMargin

  "Parsing and rendering:" - {
    "Empty input file" in {
      val replesent = REPLesent(5, 4, testcase("empty"))

      assert(captureOutput(replesent.first)._2 === empty5x4)

      assert(captureError(replesent.next)._2.nonEmpty)
    }

    "Single empty line" in {
      val replesent = REPLesent(5, 4, testcase("single_empty_line"))

      assert(captureOutput(replesent.first)._2 === empty5x4)

      assert(captureError(replesent.next)._2.nonEmpty)
    }

    "Single white space character (no EOL)" in {
      val replesent = REPLesent(5, 4, testcase("single_white_space_no_newline"))

      assert(captureOutput(replesent.first)._2 === empty5x4)

      assert(captureError(replesent.next)._2.nonEmpty)
    }

    "Single line with a single white space" in {
      val replesent = REPLesent(5, 4, testcase("single_white_space"))

      assert(captureOutput(replesent.first)._2 === empty5x4)

      assert(captureError(replesent.next)._2.nonEmpty)
    }

    "Two empty slides" in {
      val replesent = REPLesent(5, 4, testcase("two_empty_slides"))

      assert(captureOutput(replesent.first)._2 === empty5x4)

      assert(captureOutput(replesent.next)._2 === empty5x4)

      assert(captureError(replesent.next)._2.nonEmpty)
    }

    "A single slide with two empty builds" in {
      val replesent = REPLesent(5, 5, testcase("single_slide_two_empty_builds"))

      assert(captureOutput(replesent.first)._2 === empty5x5)

      assert(captureOutput(replesent.next)._2 === empty5x5)

      assert(captureError(replesent.next)._2.nonEmpty)
    }
  }
}
