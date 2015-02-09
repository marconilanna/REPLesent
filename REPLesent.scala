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
case class REPLesent(width: Int = 0, height: Int = 0, input: String = "REPLesent.txt") {
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

  // `size` and `maxLength` refer to the dimensions of the slide last build
  private case class Build(content: IndexedSeq[String], size: Int, maxLength: Int)

  private case class Slide(content: IndexedSeq[String], builds: IndexedSeq[Int]) {
    private val maxLength = content.maxBy(_.length).length

    def lastBuild: Int = builds.size - 1
    def hasBuild(n: Int): Boolean = builds.isDefinedAt(n)
    def build(n: Int): Build = Build(content.take(builds(n)), content.size, maxLength)
  }

  private case class Deck(slides: IndexedSeq[Slide]) {
    private var slideCursor = -1
    private var buildCursor = 0

    private def currentSlide: Slide = slides(slideCursor)

    private def select(slide: Int = slideCursor, build: Int = 0): Option[Build] = {
      import math.{max, min}

      // "Stops" the cursor one position after/before the last/first slide to avoid
      // multiple next/previous calls taking it indefinitely away from the deck
      slideCursor = max(-1, min(slides.size, slide))
      buildCursor = build

      if (slides.isDefinedAt(slideCursor) && currentSlide.hasBuild(buildCursor)) {
        Some(currentSlide.build(buildCursor))
      } else {
        None
      }
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

  private val config = Config(width = width, height = height)

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
      content: IndexedSeq[String] = IndexedSeq.empty
    , builds: IndexedSeq[Int] = IndexedSeq.empty
    , deck: IndexedSeq[Slide] = IndexedSeq.empty
    ) {
      def append(line: String): Acc = copy(content = content :+ line)

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

    val leftPadding = whiteSpace * ((horizontalSpace - build.maxLength) / 2)

    val sb = StringBuilder.newBuilder

    sb ++= topRow
    sb ++= blankLine * topPadding

    build.content foreach { line =>
      sb ++= sinistral + leftPadding + line

      if (dextral.nonEmpty) {
        val rightPadding = horizontalSpace - leftPadding.length - line.length
        sb ++= whiteSpace * rightPadding + dextral
      }

      sb ++= newline
    }

    sb ++= blankLine * bottomPadding
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
