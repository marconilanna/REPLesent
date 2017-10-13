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

  "Alignment - Builds:" - {
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
    def slide(i: Int): String =
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

    "Last slide" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      val slide5 = capture(replesent.last)
      assert(slide5.output === slide(5))
      assert(slide5.error.isEmpty)
    }

    "Last build" in {
      val replesent = REPLesent(w, h, testFile("navigation"))

      val slide5 = capture(replesent.Last)
      assert(slide5.output === slide(5))
      assert(slide5.error.isEmpty)
    }

    "By slides:" - {
      "Next: to first slide" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        val slide1 = capture(replesent.Next)
        assert(slide1.output === slide(1))
        assert(slide1.error.isEmpty)
      }

      "Next: from first to last" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        capture(replesent.first)

        val slide2 = capture(replesent.Next)
        assert(slide2.output === slide(2))
        assert(slide2.error.isEmpty)

        val slide3 = capture(replesent.Next)
        assert(slide3.output === slide(3))
        assert(slide3.error.isEmpty)

        val slide4 = capture(replesent.Next)
        assert(slide4.output === slide(4))
        assert(slide4.error.isEmpty)

        val slide5 = capture(replesent.Next)
        assert(slide5.output === slide(5))
        assert(slide5.error.isEmpty)
      }

      "Next: after last" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        capture(replesent.last)

        val end = capture(replesent.Next)
        assert(end.output.isEmpty)
        assert(end.error.nonEmpty)
      }

      "Previous: from last to first" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        capture(replesent.last)

        val slide4 = capture(replesent.Previous)
        assert(slide4.output === slide(4))
        assert(slide4.error.isEmpty)

        val slide3 = capture(replesent.Previous)
        assert(slide3.output === slide(3))
        assert(slide3.error.isEmpty)

        val slide2 = capture(replesent.Previous)
        assert(slide2.output === slide(2))
        assert(slide2.error.isEmpty)

        val slide1 = capture(replesent.Previous)
        assert(slide1.output === slide(1))
        assert(slide1.error.isEmpty)
      }

      "Previous: before first" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        capture(replesent.first)

        val end = capture(replesent.Previous)
        assert(end.output.isEmpty)
        assert(end.error.nonEmpty)
      }

      "Redraw" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        capture(replesent.first)

        val redrawn1 = capture(replesent.redraw)
        assert(redrawn1.output === slide(1))
        assert(redrawn1.error.isEmpty)

        capture(replesent.Next)

        val redrawn2 = capture(replesent.redraw)
        assert(redrawn2.output === slide(2))
        assert(redrawn2.error.isEmpty)

        capture(replesent.Next)

        val redrawn3 = capture(replesent.redraw)
        assert(redrawn3.output === slide(3))
        assert(redrawn3.error.isEmpty)

        capture(replesent.Next)

        val redrawn4 = capture(replesent.redraw)
        assert(redrawn4.output === slide(4))
        assert(redrawn4.error.isEmpty)

        capture(replesent.Next)

        val redrawn5 = capture(replesent.redraw)
        assert(redrawn5.output === slide(5))
        assert(redrawn5.error.isEmpty)
      }

      "Multiple next after last, then previous" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        capture(replesent.last)

        val next1 = capture(replesent.Next)
        assert(next1.output.isEmpty)
        assert(next1.error.nonEmpty)

        val next2 = capture(replesent.Next)
        assert(next2.output.isEmpty)
        assert(next2.error.nonEmpty)

        val next3 = capture(replesent.Next)
        assert(next3.output.isEmpty)
        assert(next3.error.nonEmpty)

        val previous = capture(replesent.Previous)
        assert(previous.output === slide(5))
        assert(previous.error.isEmpty)
      }

      "Multiple previous before first, then next" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        capture(replesent.first)

        val previous1 = capture(replesent.Previous)
        assert(previous1.output.isEmpty)
        assert(previous1.error.nonEmpty)

        val previous2 = capture(replesent.Previous)
        assert(previous2.output.isEmpty)
        assert(previous2.error.nonEmpty)

        val previous3 = capture(replesent.Previous)
        assert(previous3.output.isEmpty)
        assert(previous3.error.nonEmpty)

        val next = capture(replesent.Next)
        assert(next.output === slide(1))
        assert(next.error.isEmpty)
      }
    }

    "By builds:" - {
      "Next: to first slide" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        val slide1 = capture(replesent.next)
        assert(slide1.output === slide(1))
        assert(slide1.error.isEmpty)
      }

      "Next: from first to last" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        capture(replesent.first)

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

        capture(replesent.last)

        val end = capture(replesent.next)
        assert(end.output.isEmpty)
        assert(end.error.nonEmpty)
      }

      "Previous: from last to first" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        capture(replesent.last)

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

        capture(replesent.first)

        val end = capture(replesent.previous)
        assert(end.output.isEmpty)
        assert(end.error.nonEmpty)
      }

      "Multiple next after last, then previous" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        capture(replesent.last)

        val next1 = capture(replesent.next)
        assert(next1.output.isEmpty)
        assert(next1.error.nonEmpty)

        val next2 = capture(replesent.next)
        assert(next2.output.isEmpty)
        assert(next2.error.nonEmpty)

        val next3 = capture(replesent.next)
        assert(next3.output.isEmpty)
        assert(next3.error.nonEmpty)

        val previous = capture(replesent.previous)
        assert(previous.output === slide(5))
        assert(previous.error.isEmpty)
      }

      "Multiple previous before first, then next" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        capture(replesent.first)

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
    }

    "Go:" - {
      "Inside limits" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        import replesent._

        val slide2 = capture(2.go)
        assert(slide2.output === slide(2))
        assert(slide2.error.isEmpty)

        val slide5 = capture(5.go)
        assert(slide5.output === slide(5))
        assert(slide5.error.isEmpty)
      }

      "Outside limits" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        import replesent._

        val ahead = capture(99.go)
        assert(ahead.output.isEmpty)
        assert(ahead.error.nonEmpty)

        val behind = capture(-99.go)
        assert(behind.output.isEmpty)
        assert(behind.error.nonEmpty)
      }

      "Jump ahead, then previous slide" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        import replesent._

        capture(99.go)

        val slide5 = capture(replesent.Previous)
        assert(slide5.output === slide(5))
        assert(slide5.error.isEmpty)
      }

      "Jump ahead, then previous build" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        import replesent._

        capture(99.go)

        val slide5 = capture(replesent.previous)
        assert(slide5.output === slide(5))
        assert(slide5.error.isEmpty)
      }

      "Jump behind, then next slide" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        import replesent._

        capture(-99.go)

        val slide1 = capture(replesent.Next)
        assert(slide1.output === slide(1))
        assert(slide1.error.isEmpty)
      }

      "Jump behind, then next build" in {
        val replesent = REPLesent(w, h, testFile("navigation"))

        import replesent._

        capture(-99.go)

        val slide1 = capture(replesent.next)
        assert(slide1.output === slide(1))
        assert(slide1.error.isEmpty)
      }
    }
  }

  "Navigation - Builds:" - {
    def slide(a: Char, b: Char = ' '): String =
      s"""*****
         |* $a *
         |* $b *
         |*****""".stripMargin

    val (w, h) = (5, 5)

    "First" in {
      val replesent = REPLesent(w, h, testFile("navigation_build"))

      val slide1_build1 = capture(replesent.first)
      assert(slide1_build1.output === slide('a'))
      assert(slide1_build1.error.isEmpty)
    }

    "Last slide" in {
      val replesent = REPLesent(w, h, testFile("navigation_build"))

      val slide2_build1 = capture(replesent.last)
      assert(slide2_build1.output === slide('c'))
      assert(slide2_build1.error.isEmpty)
    }

    "Last build" in {
      val replesent = REPLesent(w, h, testFile("navigation_build"))

      val slide2_build2 = capture(replesent.Last)
      assert(slide2_build2.output === slide('c', 'd'))
      assert(slide2_build2.error.isEmpty)
    }

    "By slides:" - {
      "Next: to first slide" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        val slide1_build1 = capture(replesent.Next)
        assert(slide1_build1.output === slide('a'))
        assert(slide1_build1.error.isEmpty)
      }

      "Next: from first to last" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.first)

        val slide2_buid1 = capture(replesent.Next)
        assert(slide2_buid1.output === slide('c'))
        assert(slide2_buid1.error.isEmpty)
      }

      "Next: after last slide" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.last)

        val end = capture(replesent.Next)
        assert(end.output.isEmpty)
        assert(end.error.nonEmpty)
      }

      "Next: after last build" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.Last)

        val end = capture(replesent.Next)
        assert(end.output.isEmpty)
        assert(end.error.nonEmpty)
      }

      "Previous: from last slide to first" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.last)

        val slide1_build1 = capture(replesent.Previous)
        assert(slide1_build1.output === slide('a'))
        assert(slide1_build1.error.isEmpty)
      }

      "Previous: from last build to first" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.Last)

        val slide1_build1 = capture(replesent.Previous)
        assert(slide1_build1.output === slide('a'))
        assert(slide1_build1.error.isEmpty)
      }

      "Previous: before first" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.first)

        val end = capture(replesent.Previous)
        assert(end.output.isEmpty)
        assert(end.error.nonEmpty)
      }

      "Multiple next after last slide, then previous" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.last)

        val next1 = capture(replesent.Next)
        assert(next1.output.isEmpty)
        assert(next1.error.nonEmpty)

        val next2 = capture(replesent.Next)
        assert(next2.output.isEmpty)
        assert(next2.error.nonEmpty)

        val next3 = capture(replesent.Next)
        assert(next3.output.isEmpty)
        assert(next3.error.nonEmpty)

        val previous = capture(replesent.Previous)
        assert(previous.output === slide('c'))
        assert(previous.error.isEmpty)
      }

      "Multiple next after last build, then previous" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.Last)

        val next1 = capture(replesent.Next)
        assert(next1.output.isEmpty)
        assert(next1.error.nonEmpty)

        val next2 = capture(replesent.Next)
        assert(next2.output.isEmpty)
        assert(next2.error.nonEmpty)

        val next3 = capture(replesent.Next)
        assert(next3.output.isEmpty)
        assert(next3.error.nonEmpty)

        val previous = capture(replesent.Previous)
        assert(previous.output === slide('c'))
        assert(previous.error.isEmpty)
      }

      "Multiple previous before first, then next" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.first)

        val previous1 = capture(replesent.Previous)
        assert(previous1.output.isEmpty)
        assert(previous1.error.nonEmpty)

        val previous2 = capture(replesent.Previous)
        assert(previous2.output.isEmpty)
        assert(previous2.error.nonEmpty)

        val previous3 = capture(replesent.Previous)
        assert(previous3.output.isEmpty)
        assert(previous3.error.nonEmpty)

        val next = capture(replesent.Next)
        assert(next.output === slide('a'))
        assert(next.error.isEmpty)
      }
    }

    "By builds:" - {
      "Next: to first slide" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        val slide1_build1 = capture(replesent.next)
        assert(slide1_build1.output === slide('a'))
        assert(slide1_build1.error.isEmpty)
      }

      "Next: from first to last" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.first)

        val slide1_build2 = capture(replesent.next)
        assert(slide1_build2.output === slide('a', 'b'))
        assert(slide1_build2.error.isEmpty)

        val slide2_buid1 = capture(replesent.next)
        assert(slide2_buid1.output === slide('c'))
        assert(slide2_buid1.error.isEmpty)

        val slide2_build2 = capture(replesent.next)
        assert(slide2_build2.output === slide('c', 'd'))
        assert(slide2_build2.error.isEmpty)
      }

      "Next: after last slide" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.last)

        val slide2_build2 = capture(replesent.next)
        assert(slide2_build2.output === slide('c', 'd'))
        assert(slide2_build2.error.isEmpty)
      }

      "Next: after last build" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.Last)

        val end = capture(replesent.next)
        assert(end.output.isEmpty)
        assert(end.error.nonEmpty)
      }

      "Previous: from last to first" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.Last)

        val slide2_build1 = capture(replesent.previous)
        assert(slide2_build1.output === slide('c'))
        assert(slide2_build1.error.isEmpty)

        val slide1_build2 = capture(replesent.previous)
        assert(slide1_build2.output === slide('a', 'b'))
        assert(slide1_build2.error.isEmpty)

        val slide1_build1 = capture(replesent.previous)
        assert(slide1_build1.output === slide('a'))
        assert(slide1_build1.error.isEmpty)
      }

      "Previous: before first" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.first)

        val end = capture(replesent.previous)
        assert(end.output.isEmpty)
        assert(end.error.nonEmpty)
      }

      "Redraw" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.first)

        val slide1_build1 = capture(replesent.redraw)
        assert(slide1_build1.output === slide('a'))
        assert(slide1_build1.error.isEmpty)

        capture(replesent.next)

        val slide1_build2 = capture(replesent.redraw)
        assert(slide1_build2.output === slide('a', 'b'))
        assert(slide1_build2.error.isEmpty)

        capture(replesent.next)

        val slide2_buid1 = capture(replesent.redraw)
        assert(slide2_buid1.output === slide('c'))
        assert(slide2_buid1.error.isEmpty)

        capture(replesent.next)

        val slide2_build2 = capture(replesent.redraw)
        assert(slide2_build2.output === slide('c', 'd'))
        assert(slide2_build2.error.isEmpty)
      }

      "Multiple next after last, then previous" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.Last)

        val next1 = capture(replesent.next)
        assert(next1.output.isEmpty)
        assert(next1.error.nonEmpty)

        val next2 = capture(replesent.next)
        assert(next2.output.isEmpty)
        assert(next2.error.nonEmpty)

        val next3 = capture(replesent.next)
        assert(next3.output.isEmpty)
        assert(next3.error.nonEmpty)

        val previous = capture(replesent.previous)
        assert(previous.output === slide('c', 'd'))
        assert(previous.error.isEmpty)
      }

      "Multiple previous before first, then next" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        capture(replesent.first)

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
        assert(next.output === slide('a'))
        assert(next.error.isEmpty)
      }
    }

    "Go:" - {
      "Inside limits" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        import replesent._

        val slide1_build1 = capture(1.go)
        assert(slide1_build1.output === slide('a'))
        assert(slide1_build1.error.isEmpty)

        val slide2_build1 = capture(2.go)
        assert(slide2_build1.output === slide('c'))
        assert(slide2_build1.error.isEmpty)
      }

      "Outside limits" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        import replesent._

        val ahead = capture(99.go)
        assert(ahead.output.isEmpty)
        assert(ahead.error.nonEmpty)

        val behind = capture(-99.go)
        assert(behind.output.isEmpty)
        assert(behind.error.nonEmpty)
      }

      "Jump ahead, then previous slide" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        import replesent._

        capture(99.go)

        val slide2_build1 = capture(replesent.Previous)
        assert(slide2_build1.output === slide('c'))
        assert(slide2_build1.error.isEmpty)
      }

      "Jump ahead, then previous build" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        import replesent._

        capture(99.go)

        val slide2_build2 = capture(replesent.previous)
        assert(slide2_build2.output === slide('c', 'd'))
        assert(slide2_build2.error.isEmpty)
      }

      "Jump behind, then next slide" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        import replesent._

        capture(-99.go)

        val slide1_build1 = capture(replesent.Next)
        assert(slide1_build1.output === slide('a'))
        assert(slide1_build1.error.isEmpty)
      }

      "Jump behind, then next build" in {
        val replesent = REPLesent(w, h, testFile("navigation_build"))

        import replesent._

        capture(-99.go)

        val slide1_build1 = capture(replesent.next)
        assert(slide1_build1.output === slide('a'))
        assert(slide1_build1.error.isEmpty)
      }
    }
  }

  "ANSI Colors" in {
    import scala.io.AnsiColor._

    val expected =
      s"""***************************
        |* $RED red $GREEN green $BLUE blue$RESET       *
        |* $CYAN cyan $MAGENTA magenta $YELLOW yellow$RESET  *
        |* $BLACK black $WHITE white$RESET           *
        |*                         *
        |* $BOLD$RED red $GREEN green $BLUE blue$RESET       *
        |* $BOLD$CYAN cyan $MAGENTA magenta $YELLOW yellow$RESET  *
        |* $BOLD$BLACK black $WHITE white$RESET           *
        |*                         *
        |* $RED_B red $GREEN_B green $BLUE_B blue$RESET       *
        |* $CYAN_B cyan $MAGENTA_B magenta $YELLOW_B yellow$RESET  *
        |* $BLACK_B black $WHITE_B white$RESET           *
        |*                         *
        |* ${UNDERLINED}underline$RESET reset ${REVERSED}reverse$RESET *
        |*                         *
        |* \\r escaped              *
        |* \\$RED red$RESET                   *
        |* \\\\r escaped             *
        |* \\z unaffected           *
        |***************************""".stripMargin

    val replesent = REPLesent(27, 21, testFile("colors"))

    val colors = capture(replesent.first)
    assert(colors.output === expected)
    assert(colors.error.isEmpty)
  }

  "Emoji" in {
    val expected =
      """*************
        |* 👍 👍 👏 😄   *
        |*  🍌 🍌 🍌 🍌  *
        |*           *
        |* 🌎 🌍 🌏     *
        |*           *
        |* :invalid: *
        |*************""".stripMargin

    val replesent = REPLesent(13, 9, testFile("emoji"))

    val emoji = capture(replesent.first)
    assert(emoji.output === expected)
    assert(emoji.error.isEmpty)
  }

  "Line alignment:" - {
    "Odd horizontal space" in {
      val expected1 =
        """*************************
          |*   a very long line    *
          |* left flushed          *
          |*   forced left         *
          |*   default left        *
          |*       centered        *
          |*       centered!       *
          |*              right    *
          |*         right flushed *
          |*************************""".stripMargin

      val expected2 =
        """*************************
          |*   a very long line!   *
          |* left flushed          *
          |*   forced left         *
          |*   default left        *
          |*       centered        *
          |*       centered!       *
          |*               right   *
          |*         right flushed *
          |*************************""".stripMargin

      val replesent = REPLesent(25, 11, testFile("line_alignment"))

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)
      assert(slide1.error.isEmpty)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)
      assert(slide2.error.isEmpty)
    }

    "Even horizontal space" in {
      val expected1 =
        """**************************
          |*    a very long line    *
          |* left flushed           *
          |*    forced left         *
          |*    default left        *
          |*        centered        *
          |*       centered!        *
          |*               right    *
          |*          right flushed *
          |**************************""".stripMargin

      val expected2 =
        """**************************
          |*   a very long line!    *
          |* left flushed           *
          |*   forced left          *
          |*   default left         *
          |*        centered        *
          |*       centered!        *
          |*               right    *
          |*          right flushed *
          |**************************""".stripMargin

      val replesent = REPLesent(26, 11, testFile("line_alignment"))

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)
      assert(slide1.error.isEmpty)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)
      assert(slide2.error.isEmpty)
    }
  }

  "Horizontal Ruler:" - {
    import scala.io.AnsiColor._

    "Full Screen" in {
      val (w, h) = (16, 3)

      val expected1 =
        """****************
          |* ------------ *
          |****************""".stripMargin

      val expected2 =
        s"""****************
          |* $RED-=$BLUE-=-$RESET$RED-=$BLUE-=-$RESET$RED-=$RESET *
          |****************""".stripMargin

      val expected3 =
        """****************
          |* -+--+--+--+- *
          |****************""".stripMargin

      val replesent = REPLesent(w, h, testFile("horizontal_ruler_full_screen"))

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)
      assert(slide1.error.isEmpty)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)
      assert(slide2.error.isEmpty)

      val slide3 = capture(replesent.next)
      assert(slide3.output === expected3)
      assert(slide3.error.isEmpty)
    }

    "Slide" in {
      val (w, h) = (16, 5)

      val expected1 =
        """****************
          |*    lipsum    *
          |*    ------    *
          |****************""".stripMargin

      val expected2 =
        s"""****************
          |*    lipsum    *
          |*    $RED-=$BLUE-=-$RESET$RED-$RESET    *
          |****************""".stripMargin

      val expected3 =
        """****************
          |*    lipsum    *
          |*    -+--+-    *
          |****************""".stripMargin

      val replesent = REPLesent(w, h, testFile("horizontal_ruler_slide"))

      val slide1 = capture(replesent.first)
      assert(slide1.output === expected1)
      assert(slide1.error.isEmpty)

      val slide2 = capture(replesent.next)
      assert(slide2.output === expected2)
      assert(slide2.error.isEmpty)

      val slide3 = capture(replesent.next)
      assert(slide3.output === expected3)
      assert(slide3.error.isEmpty)
    }
  }

  "Slide counter:" - {
    "Without total" in {
      val replesent = REPLesent(7, 6, testFile("slide_counter"), true)

      val slide1 = capture(replesent.first)
      assert(slide1.output contains "*  1  *")

      val slide2 = capture(replesent.next)
      assert(slide2.output contains "*  2  *")

      val slide3 = capture(replesent.next)
      assert(slide3.output contains "*  2  *")

      val slide4 = capture(replesent.next)
      assert(slide4.output contains "*  3  *")
    }

    "With total" in {
      val replesent = REPLesent(9, 6, testFile("slide_counter"), true, true)

      val slide1 = capture(replesent.first)
      assert(slide1.output contains "*  1/3  *")

      val slide2 = capture(replesent.next)
      assert(slide2.output contains "*  2/3  *")

      val slide3 = capture(replesent.next)
      assert(slide3.output contains "*  2/3  *")

      val slide4 = capture(replesent.next)
      assert(slide4.output contains "*  3/3  *")
    }
  }
}
