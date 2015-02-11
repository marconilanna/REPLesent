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
case class REPLesent(
  width: Int = 0
, height: Int = 0
, input: String = "REPLesent.txt"
, slideCounter: Boolean = false
, slideTotal: Boolean = false
) {
  import scala.util.Try

  private case class Config(
    top: String = "*"
  , bottom: String = "*"
  , sinistral: String = "* "
  , dextral: String = " *"
  , newline: String = System.lineSeparator
  , private val width: Int
  , private val height: Int
  ) {
    val (screenWidth, screenHeight): (Int, Int) = {
      val defaultWidth = 80
      val defaultHeight = 25

      if (width > 0 && height > 0) (width, height) else {
        // Experimental support for screen size auto-detection.
        // Supports only Unix-like systems, including Mac OS X and Linux.
        // Does not work with Microsoft Windows.
        val Array(h, w) = Try {
          import scala.sys.process._

          val stty = Seq("sh", "-c", "stty size < /dev/tty").!!

          stty.trim.split(' ') map { _.toInt }
        } getOrElse Array(0, 0)

        val screenWidth = Seq(width, w) find { _ > 0 } getOrElse defaultWidth
        val screenHeight = Seq(height, h) find { _ > 0 } getOrElse defaultHeight

        (screenWidth, screenHeight)
      }
    }

    private def fill(s: String): String = if (s.isEmpty) s else {
      val t = s * (screenWidth / s.length)
      t + s.take(screenWidth - t.length)
    }

    val topRow = fill(top) + newline
    val bottomRow = fill(bottom)

    val verticalSpace = screenHeight - 3 // accounts for header, footer, and REPL prompt
    val horizontalSpace = screenWidth - sinistral.length - dextral.length

    val whiteSpace = " "

    val blankLine = sinistral + {
      if (dextral.isEmpty) "" else whiteSpace * horizontalSpace + dextral
    } + newline
  }

  private val config = Config(width = width, height = height)

  private case class Line(content: String, length: Int, private val alignment: Line.Alignment) {
    override def toString = content
    def alignTo(margin: Int): String = alignment(this, margin)
  }

  private object Line {
    protected sealed trait Alignment {
      private def blank = config.whiteSpace

      protected def space = config.horizontalSpace

      protected def fill(line: Line, left: Int, right: Int): String = {
        blank * left  + line + blank * right
      }

      def apply(line: Line, margin: Int): String
    }

    object LeftFlushed extends Alignment {
      def apply(line: Line, ignored: Int): String = {
        val left = 0
        val right = space - line.length

        fill(line, left, right)
      }
    }

    object LeftAligned extends Alignment {
      def apply(line: Line, margin: Int): String = {
        val left = margin / 2
        val right = space - left - line.length

        fill(line, left, right)
      }
    }

    object Centered extends Alignment {
      def apply(line: Line, ignored: Int): String = {
        val margin = space - line.length

        val left = margin / 2
        val right = margin - left

        fill(line, left, right)
      }
    }

    object RightAligned extends Alignment {
      def apply(line: Line, margin: Int): String = {
        val right = (margin + 1) / 2
        val left = space - right - line.length

        fill(line, left, right)
      }
    }

    object RightFlushed extends Alignment {
      def apply(line: Line, ignored: Int): String = {
        val left = space - line.length
        val right = 0

        fill(line, left, right)
      }
    }

    private val colorEscape = """\\.""".r

    def apply(line: String): Line = {
      import scala.io.AnsiColor._

      val (_line, alignment): (String, Alignment) = line match {
        case s if s startsWith "<< " => (s.drop(3), LeftFlushed)
        case s if s startsWith "< " => (s.drop(2), LeftAligned)
        case s if s startsWith "| " => (s.drop(2), Centered)
        case s if s startsWith "> " => (s.drop(2), RightAligned)
        case s if s startsWith ">> " => (s.drop(3), RightFlushed)
        case s => (s, LeftAligned)
      }

      var length = _line.length
      var ansi = false

      def color(c: String) = { length -= 2; ansi = true; c }

      val content: String = colorEscape replaceAllIn (_line, m => m.matched(1) match {
        case '\\' => length -= 1; "\\\\"
        case 'b' => color(BLUE)
        case 'B' => color(BLUE_B)
        case 'c' => color(CYAN)
        case 'C' => color(CYAN_B)
        case 'g' => color(GREEN)
        case 'G' => color(GREEN_B)
        case 'k' => color(BLACK)
        case 'K' => color(BLACK_B)
        case 'm' => color(MAGENTA)
        case 'M' => color(MAGENTA_B)
        case 'r' => color(RED)
        case 'R' => color(RED_B)
        case 's' => length -= 2; RESET
        case 'w' => color(WHITE)
        case 'W' => color(WHITE_B)
        case 'y' => color(YELLOW)
        case 'Y' => color(YELLOW_B)
        case '!' => color(REVERSED)
        case '*' => color(BOLD)
        case '_' => color(UNDERLINED)
        case c => "\\\\" + c
      })

      val reset = if (ansi) RESET else ""

      Line(content = content + reset, length = length, alignment = alignment)
    }
  }

  // `size` and `maxLength` refer to the dimensions of the slide last build
  private case class Build(content: IndexedSeq[Line], size: Int, maxLength: Int, footer: Line)

  private case class Slide(content: IndexedSeq[Line], builds: IndexedSeq[Int]) {
    private val maxLength = content.maxBy(_.length).length

    def lastBuild: Int = builds.size - 1
    def hasBuild(n: Int): Boolean = builds.isDefinedAt(n)
    def build(n: Int, footer: Line): Build = Build(content.take(builds(n)), content.size, maxLength, footer)
  }

  private case class Deck(slides: IndexedSeq[Slide]) {
    private var slideCursor = -1
    private var buildCursor = 0

    private def currentSlideIsDefined: Boolean = slides.isDefinedAt(slideCursor)
    private def currentSlide: Slide = slides(slideCursor)

    private def footer: Line = {
      val sb = StringBuilder.newBuilder
      if (slideCounter) {
        sb ++= ">> " + (slideCursor + 1)

        if (slideTotal) sb ++= "/" + slides.size

        sb ++= " "
      }
      Line(sb.mkString)
    }

    private def select(slide: Int = slideCursor, build: Int = 0): Option[Build] = {
      import math.{max, min}

      // "Stops" the cursor one position after/before the last/first slide to avoid
      // multiple next/previous calls taking it indefinitely away from the deck
      slideCursor = max(-1, min(slides.size, slide))
      buildCursor = build

      if (currentSlideIsDefined && currentSlide.hasBuild(buildCursor)) {
        Some(currentSlide.build(buildCursor, footer))
      } else None
    }

    def jumpTo(n: Int): Option[Build] = select(slide = n)

    def jump(n: Int): Option[Build] = jumpTo(slideCursor + n)

    def nextBuild: Option[Build] = select(build = buildCursor + 1) orElse jump(1)

    def previousBuild: Option[Build] = select(build = buildCursor - 1) orElse {
      jump(-1) flatMap { _ =>
        select(build = currentSlide.lastBuild)
      }
    }

    def lastSlide: Option[Build] = jumpTo(slides.size - 1)

    def lastBuild: Option[Build] = jumpTo(slides.size) orElse previousBuild
  }

  private val helpMessage = """Usage:
    |  next          n      >     go to next build/slide
    |  previous      p      <     go back to previous build/slide
    |  Next          N      >>    go to next slide
    |  Previous      P      <<    go back to previous slide
    |  i next        i n          advance i slides
    |  i previous    i p          go back i slides
    |  i go          i g          go to slide i
    |  first         f      |<    go to first slide
    |  last          l      >|    go to last slide
    |  Last          L      >>|   go to last build of last slide
    |  blank         b            blank screen
    |  help          h      ?     print this help message""".stripMargin

  private val deck = Deck(parseFile(input))

  private def parseFile(file: String): IndexedSeq[Slide] = {
    Try {
      val input = io.Source.fromFile(file).getLines
      parse(input)
    } getOrElse {
      Console.err.print(s"Sorry, could not parse file $file. Quick, say something funny before anyone notices!")
      IndexedSeq.empty
    }
  }

  private def parse(input: Iterator[String]): IndexedSeq[Slide] = {
    case class Acc(
      content: IndexedSeq[Line] = IndexedSeq.empty
    , builds: IndexedSeq[Int] = IndexedSeq.empty
    , deck: IndexedSeq[Slide] = IndexedSeq.empty
    ) {
      def append(line: String): Acc = copy(content = content :+ Line(line))

      def pushBuild: Acc = copy(builds = builds :+ content.size)

      def pushSlide: Acc = {
        if (content.isEmpty) {
          append("").pushSlide
        } else {
          val builds = pushBuild.builds
          val slide = Slide(content, builds)

          Acc(deck = deck :+ slide)
        }
      }
    }

    val slideSeparator = "---"
    val buildSeparator = "--"

    val acc = (Acc() /: input) { (acc, line) =>
      line match {
        case `slideSeparator` => acc.pushSlide
        case `buildSeparator` => acc.pushBuild
        case _ => acc.append(line)
      }
    }.pushSlide

    acc.deck
  }

  private def render(build: Build): String = {
    import config._

    val topPadding = (verticalSpace - build.size) / 2
    val bottomPadding = verticalSpace - topPadding - build.content.size

    val margin = horizontalSpace - build.maxLength

    val sb = StringBuilder.newBuilder

    def render(line: Line) = {
      sb ++= sinistral
      sb ++= line.alignTo(margin)
      sb ++= dextral
      sb ++= newline
    }

    sb ++= topRow
    sb ++= blankLine * topPadding

    build.content foreach render

    if (slideCounter && bottomPadding > 0) {
      sb ++= blankLine * (bottomPadding - 1)
      render(build.footer)
    } else {
      sb ++= blankLine * bottomPadding
    }

    sb ++= bottomRow

    sb.mkString
  }

  private def show(build: Option[Build]): Unit = {
    if (build.isEmpty) Console.err.print("No slide for you")

    build foreach { b => print(render(b)) }
  }

  implicit class Ops(val i: Int) {
    def next: Unit = show(deck.jump(i))
    def n: Unit = next

    def previous: Unit = show(deck.jump(-i))
    def p: Unit = previous

    def go: Unit = show(deck.jumpTo(i - 1))
    def g: Unit = go
  }

  def next: Unit = show(deck.nextBuild)
  def n: Unit = next
  def > : Unit = next

  def previous: Unit = show(deck.previousBuild)
  def p: Unit = previous
  def < : Unit = previous

  def Next: Unit = 1.next
  def N: Unit = Next
  def >> : Unit = Next

  def Previous: Unit = 1.previous
  def P: Unit = Previous
  def << : Unit = Previous

  def first: Unit = 1.go
  def f: Unit = first
  def |< : Unit = first

  def last: Unit = show(deck.lastSlide)
  def l: Unit = last
  def >| : Unit = last

  def Last: Unit = show(deck.lastBuild)
  def L: Unit = Last
  def >>| : Unit = Last

  def blank: Unit = print(config.newline * config.screenHeight)
  def b: Unit = blank

  def help: Unit = print(helpMessage)
  def h: Unit = help
  def ? : Unit = help
}
