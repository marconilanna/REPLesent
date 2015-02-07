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
  case class Capture[A](result: A, output: String, error: String)

  def capture[A](f: => A): Capture[A] = {
    val output = new java.io.ByteArrayOutputStream
    val error = new java.io.ByteArrayOutputStream

    val result = Console.withOut(output) {
      Console.withErr(error) {
        f
      }
    }

    Capture(result = result, output = output.toString, error = error.toString)
  }

  def testFile(file: String): String = getClass.getResource(s"/test_$file.txt").getPath

  val testWidth = 6
  val testHeight = 6

  val emptySlide =
    """******
      |*    *
      |*    *
      |*    *
      |******""".stripMargin

  "Parsing and rendering:" - {
    "Empty input file" in {
      val parse = capture(REPLesent(testWidth, testHeight, testFile("empty")))
      assert(parse.output.isEmpty)
      assert(parse.error.isEmpty)

      val replesent = parse.result

      val slide1 = capture(replesent.first)
      assert(slide1.output === emptySlide)
      assert(slide1.error.isEmpty)

      val end = capture(replesent.next)
      assert(end.output.isEmpty)
      assert(end.error.nonEmpty)
    }

    "Single empty line" in {
      val parse = capture(REPLesent(testWidth, testHeight, testFile("single_empty_line")))
      assert(parse.output.isEmpty)
      assert(parse.error.isEmpty)

      val replesent = parse.result

      val slide1 = capture(replesent.first)
      assert(slide1.output === emptySlide)
      assert(slide1.error.isEmpty)

      val end = capture(replesent.next)
      assert(end.output.isEmpty)
      assert(end.error.nonEmpty)
    }

    "Single white space character (no EOL)" in {
      val parse = capture(REPLesent(testWidth, testHeight, testFile("single_white_space_no_newline")))
      assert(parse.output.isEmpty)
      assert(parse.error.isEmpty)

      val replesent = parse.result

      val slide1 = capture(replesent.first)
      assert(slide1.output === emptySlide)
      assert(slide1.error.isEmpty)

      val end = capture(replesent.next)
      assert(end.output.isEmpty)
      assert(end.error.nonEmpty)
    }

    "Single line with a single white space" in {
      val parse = capture(REPLesent(testWidth, testHeight, testFile("single_white_space")))
      assert(parse.output.isEmpty)
      assert(parse.error.isEmpty)

      val replesent = parse.result

      val slide1 = capture(replesent.first)
      assert(slide1.output === emptySlide)
      assert(slide1.error.isEmpty)

      val end = capture(replesent.next)
      assert(end.output.isEmpty)
      assert(end.error.nonEmpty)
    }

    "Two empty slides" in {
      val parse = capture(REPLesent(testWidth, testHeight, testFile("two_empty_slides")))
      assert(parse.output.isEmpty)
      assert(parse.error.isEmpty)

      val replesent = parse.result

      val slide1 = capture(replesent.first)
      assert(slide1.output === emptySlide)
      assert(slide1.error.isEmpty)

      val slide2 = capture(replesent.next)
      assert(slide2.output === emptySlide)
      assert(slide2.error.isEmpty)

      val end = capture(replesent.next)
      assert(end.output.isEmpty)
      assert(end.error.nonEmpty)
    }

    "A single slide with two empty builds" in {
      val parse = capture(REPLesent(testWidth, testHeight, testFile("single_slide_two_empty_builds")))
      assert(parse.output.isEmpty)
      assert(parse.error.isEmpty)

      val replesent = parse.result

      val slide1 = capture(replesent.first)
      assert(slide1.output === emptySlide)
      assert(slide1.error.isEmpty)

      val slide2 = capture(replesent.next)
      assert(slide2.output === emptySlide)
      assert(slide2.error.isEmpty)

      val end = capture(replesent.next)
      assert(end.output.isEmpty)
      assert(end.error.nonEmpty)
    }

    "A minimal non-empty slide" in {
      val (w, h) = (5, 4)

      val expected =
        """*****
          |* a *
          |*****""".stripMargin

      val parse = capture(REPLesent(w, h, testFile("simple_slide")))
      assert(parse.output.isEmpty)
      assert(parse.error.isEmpty)

      val replesent = parse.result

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected)
      assert(slide1.error.isEmpty)

      val end = capture(replesent.next)
      assert(end.output.isEmpty)
      assert(end.error.nonEmpty)
    }

    "A minimal slide with two non-empty builds" in {
      val (w, h) = (5, 5)

      val expected1 =
        """*****
          |* a *
          |*   *
          |*****""".stripMargin

      val expected2 =
        """*****
          |* a *
          |* b *
          |*****""".stripMargin

      val parse = capture(REPLesent(w, h, testFile("simple_build")))
      assert(parse.output.isEmpty)
      assert(parse.error.isEmpty)

      val replesent = parse.result

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)
      assert(slide1.error.isEmpty)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)
      assert(slide2.error.isEmpty)

      val end = capture(replesent.next)
      assert(end.output.isEmpty)
      assert(end.error.nonEmpty)
    }

    "A slide with two builds followed by another slide" in {
      val (w, h) = (5, 5)

      val expected1 =
        """*****
          |* a *
          |*   *
          |*****""".stripMargin

      val expected2 =
        """*****
          |* a *
          |* b *
          |*****""".stripMargin

      val expected3 =
        """*****
          |* c *
          |*   *
          |*****""".stripMargin

      val parse = capture(REPLesent(w, h, testFile("two_builds_and_a_slide")))
      assert(parse.output.isEmpty)
      assert(parse.error.isEmpty)

      val replesent = parse.result

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)
      assert(slide1.error.isEmpty)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)
      assert(slide2.error.isEmpty)

      val slide3 = capture(replesent.next)
      assert(slide3.output === expected3)
      assert(slide3.error.isEmpty)

      val end = capture(replesent.next)
      assert(end.output.isEmpty)
      assert(end.error.nonEmpty)
    }
  }

  "Alignment - Slides:" - {
    "Minimal screen dimensions" in {
      val (w, h) = (7, 6)

      val expected1 =
        """*******
          |*     *
          |*  a  *
          |*     *
          |*******""".stripMargin

      val expected2 =
        """*******
          |* bb  *
          |* bb  *
          |*     *
          |*******""".stripMargin

      val expected3 =
        """*******
          |* ccc *
          |* ccc *
          |* ccc *
          |*******""".stripMargin

      val expected4 =
        """*******
          |* bb  *
          |* ccc *
          |* a   *
          |*******""".stripMargin

      val replesent = REPLesent(w, h, testFile("slide_alignment"))

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)

      val slide3 = capture(replesent.next)
      assert(slide3.output === expected3)

      val slide4 = capture(replesent.next)
      assert(slide4.output === expected4)
    }

    "Even extra space" in {
      val (w, h) = (9, 8)

      val expected1 =
        """*********
          |*       *
          |*       *
          |*   a   *
          |*       *
          |*       *
          |*********""".stripMargin

      val expected2 =
        """*********
          |*       *
          |*  bb   *
          |*  bb   *
          |*       *
          |*       *
          |*********""".stripMargin

      val expected3 =
        """*********
          |*       *
          |*  ccc  *
          |*  ccc  *
          |*  ccc  *
          |*       *
          |*********""".stripMargin

      val expected4 =
        """*********
          |*       *
          |*  bb   *
          |*  ccc  *
          |*  a    *
          |*       *
          |*********""".stripMargin

      val replesent = REPLesent(w, h, testFile("slide_alignment"))

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)

      val slide3 = capture(replesent.next)
      assert(slide3.output === expected3)

      val slide4 = capture(replesent.next)
      assert(slide4.output === expected4)
    }

    "Odd extra space" in {
      val (w, h) = (10, 9)

      val expected1 =
        """**********
          |*        *
          |*        *
          |*   a    *
          |*        *
          |*        *
          |*        *
          |**********""".stripMargin

      val expected2 =
        """**********
          |*        *
          |*        *
          |*   bb   *
          |*   bb   *
          |*        *
          |*        *
          |**********""".stripMargin

      val expected3 =
        """**********
          |*        *
          |*  ccc   *
          |*  ccc   *
          |*  ccc   *
          |*        *
          |*        *
          |**********""".stripMargin

      val expected4 =
        """**********
          |*        *
          |*  bb    *
          |*  ccc   *
          |*  a     *
          |*        *
          |*        *
          |**********""".stripMargin

      val replesent = REPLesent(w, h, testFile("slide_alignment"))

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)

      val slide3 = capture(replesent.next)
      assert(slide3.output === expected3)

      val slide4 = capture(replesent.next)
      assert(slide4.output === expected4)
    }
  }

  "Alignment - Build:" - {
    "Minimal screen dimensions" in {
      val (w, h) = (7, 6)

      val expected1 =
        """*******
          |* bb  *
          |*     *
          |*     *
          |*******""".stripMargin

      val expected2 =
        """*******
          |* bb  *
          |* ccc *
          |*     *
          |*******""".stripMargin

      val expected3 =
        """*******
          |* bb  *
          |* ccc *
          |* a   *
          |*******""".stripMargin

      val replesent = REPLesent(w, h, testFile("build_alignment"))

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)

      val slide3 = capture(replesent.next)
      assert(slide3.output === expected3)
    }

    "Even extra space" in {
      val (w, h) = (9, 8)

      val expected1 =
        """*********
          |*       *
          |*  bb   *
          |*       *
          |*       *
          |*       *
          |*********""".stripMargin

      val expected2 =
        """*********
          |*       *
          |*  bb   *
          |*  ccc  *
          |*       *
          |*       *
          |*********""".stripMargin

      val expected3 =
        """*********
          |*       *
          |*  bb   *
          |*  ccc  *
          |*  a    *
          |*       *
          |*********""".stripMargin

      val replesent = REPLesent(w, h, testFile("build_alignment"))

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)

      val slide3 = capture(replesent.next)
      assert(slide3.output === expected3)
    }

    "Odd extra space" in {
      val (w, h) = (10, 9)

      val expected1 =
        """**********
          |*        *
          |*  bb    *
          |*        *
          |*        *
          |*        *
          |*        *
          |**********""".stripMargin

      val expected2 =
        """**********
          |*        *
          |*  bb    *
          |*  ccc   *
          |*        *
          |*        *
          |*        *
          |**********""".stripMargin

      val expected3 =
        """**********
          |*        *
          |*  bb    *
          |*  ccc   *
          |*  a     *
          |*        *
          |*        *
          |**********""".stripMargin

      val replesent = REPLesent(w, h, testFile("build_alignment"))

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)

      val slide3 = capture(replesent.next)
      assert(slide3.output === expected3)
    }
  }

  "Navigation - Slides:" - {
    def slide(i: Int) =
      s"""*****
         |* $i *
         |*****""".stripMargin

    val (w, h) = (5, 4)

    "First" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      val slide1 = capture(replesent.first)
      assert(slide1.output === slide(1))
      assert(slide1.error.isEmpty)
    }

    "Last" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      val slide5 = capture(replesent.last)
      assert(slide5.output === slide(5))
      assert(slide5.error.isEmpty)
    }

    "Next: to first slide" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      val slide1 = capture(replesent.next)
      assert(slide1.output === slide(1))
      assert(slide1.error.isEmpty)
    }

    "Next: from first to last" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      replesent.first

      val slide2 = capture(replesent.next)
      assert(slide2.output === slide(2))
      assert(slide2.error.isEmpty)

      val slide3 = capture(replesent.next)
      assert(slide3.output === slide(3))
      assert(slide3.error.isEmpty)

      val slide4 = capture(replesent.next)
      assert(slide4.output === slide(4))
      assert(slide4.error.isEmpty)

      val slide5 = capture(replesent.next)
      assert(slide5.output === slide(5))
      assert(slide5.error.isEmpty)
    }

    "Next: after last" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      replesent.last

      val end = capture(replesent.next)
      assert(end.output.isEmpty)
      assert(end.error.nonEmpty)
    }

    "Previous: from last to first" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      replesent.last

      val slide4 = capture(replesent.previous)
      assert(slide4.output === slide(4))
      assert(slide4.error.isEmpty)

      val slide3 = capture(replesent.previous)
      assert(slide3.output === slide(3))
      assert(slide3.error.isEmpty)

      val slide2 = capture(replesent.previous)
      assert(slide2.output === slide(2))
      assert(slide2.error.isEmpty)

      val slide1 = capture(replesent.previous)
      assert(slide1.output === slide(1))
      assert(slide1.error.isEmpty)
    }

    "Previous: before first" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      replesent.first

      val end = capture(replesent.previous)
      assert(end.output.isEmpty)
      assert(end.error.nonEmpty)
    }

    "Multiple next after last, then previous" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      replesent.last

      val next1 = capture(replesent.next)
      assert(next1.output.isEmpty)
      assert(next1.error.nonEmpty)

      val next2 = capture(replesent.next)
      assert(next2.output.isEmpty)
      assert(next2.error.nonEmpty)

      val next3 = capture(replesent.next)
      assert(next3.output.isEmpty)
      assert(next3.error.nonEmpty)

      val next = capture(replesent.previous)
      assert(next.output === slide(5))
      assert(next.error.isEmpty)
    }

    "Multiple previous before first, then next" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      replesent.first

      val previous1 = capture(replesent.previous)
      assert(previous1.output.isEmpty)
      assert(previous1.error.nonEmpty)

      val previous2 = capture(replesent.previous)
      assert(previous2.output.isEmpty)
      assert(previous2.error.nonEmpty)

      val previous3 = capture(replesent.previous)
      assert(previous3.output.isEmpty)
      assert(previous3.error.nonEmpty)

      val next = capture(replesent.next)
      assert(next.output === slide(1))
      assert(next.error.isEmpty)
    }

    "Go: inside limits" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      import replesent._

      val slide2 = capture(2.go)
      assert(slide2.output === slide(2))
      assert(slide2.error.isEmpty)

      val slide5 = capture(5.go)
      assert(slide5.output === slide(5))
      assert(slide5.error.isEmpty)
    }

    "Go: outside limits" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      import replesent._

      val ahead = capture(99.go)
      assert(ahead.output.isEmpty)
      assert(ahead.error.nonEmpty)

      val behind = capture(-99.go)
      assert(behind.output.isEmpty)
      assert(behind.error.nonEmpty)
    }

    "Go: jump ahead, then previous" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      import replesent._

      99.go

      val slide5 = capture(replesent.previous)
      assert(slide5.output === slide(5))
      assert(slide5.error.isEmpty)
    }

    "Go: jump behind, then next" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      import replesent._

      -99.go

      val slide1 = capture(replesent.next)
      assert(slide1.output === slide(1))
      assert(slide1.error.isEmpty)
    }
  }
}
