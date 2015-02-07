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
  }

  // For slides that are part of a build, `size` and `maxLength` refer to the dimensions
  // of the last step of the build and may differ from the slide `content` dimensions.
  private case class Slide(content: IndexedSeq[String], size: Int, maxLength: Int)

  private val helpMessage = """Usage:
    |  next          n      >     next slide
    |  previous      p      <     previous slide
    |  i next        i n          advance i slides
    |  i previous    i p          go back i slides
    |  i go          i g          go to slide i
    |  first         f      <<    go to first slide
    |  last          l      >>    go to last slide
    |  blank         b            blank screen
    |  help          h      ?     print this help message""".stripMargin

  private val config = Config(width = width, height = height)

  private val deck: IndexedSeq[Slide] = parseFile(input)

  private var cursor = -1

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
    , fragments: IndexedSeq[Int] = IndexedSeq.empty
    , deck: IndexedSeq[Slide] = IndexedSeq.empty
    ) {
      def append(line: String): Acc = copy(content = content :+ line)

      def pushFragment: Acc = copy(fragments = fragments :+ content.size)

      def pushSlide: Acc = {
        if (content.isEmpty) {
          append("").pushSlide
        } else {
          val fragments = pushFragment.fragments
          val maxLength = content.maxBy(_.length).length
          val slides = fragments map { n =>
            Slide(content.take(n), size = content.size, maxLength = maxLength)
          }

          Acc(deck = deck ++ slides)
        }
      }
    }

    val slideSeparator = "---"
    val fragmentSeparator = "--"

    val acc = (Acc() /: input) { (acc, line) =>
      line match {
        case `slideSeparator` => acc.pushSlide
        case `fragmentSeparator` => acc.pushFragment
        case _ => acc.append(line)
      }
    }.pushSlide

    acc.deck
  }

  private def render(slide: Slide): String = {
    import config._

    def fill(s: String): String = if (s.isEmpty) s else {
      val t = s * (screenWidth / s.length)
      t + s.take(screenWidth - t.length)
    }

    val separator = " "

    val verticalSpace = screenHeight - 3 // accounts for header, footer, and REPL prompt
    val horizontalSpace = screenWidth - sinistral.length - dextral.length

    val topPadding = (verticalSpace - slide.size) / 2
    val bottomPadding = verticalSpace - topPadding - slide.content.size

    val leftPadding = separator * ((horizontalSpace - slide.maxLength) / 2)

    val blank = sinistral + {
      if (dextral.isEmpty) "" else separator * horizontalSpace + dextral
    } + newline

    val sb = StringBuilder.newBuilder

    sb ++= fill(top) + newline
    sb ++= blank * topPadding

    slide.content foreach { line =>
      sb ++= sinistral + leftPadding + line

      if (dextral.nonEmpty) {
        val rightPadding = horizontalSpace - leftPadding.length - line.length
        sb ++= separator * rightPadding + dextral
      }

      sb ++= newline
    }

    sb ++= blank * bottomPadding
    sb ++= fill(bottom)

    sb.mkString
  }

  private def display(n: Int): Unit = {
    if (deck.isDefinedAt(n)) {
      val slide = render(deck(n))
      print(slide)
    } else {
      Console.err.print("No slide for you")
    }
  }

  private def jumpTo(n: Int): Unit = {
    import math.{max, min}

    // "Stops" the cursor one position after/before the last/first slide to avoid
    // multiple next/previous calls taking it indefinitely away from the deck
    cursor =  max(-1, min(deck.size, n))

    display(cursor)
  }

  implicit class Ops(val i: Int) {
    def next: Unit = jumpTo(cursor + i)
    def n: Unit = next

    def previous: Unit = jumpTo(cursor - i)
    def p: Unit = previous

    def go: Unit = jumpTo(i - 1)
    def g: Unit = go
  }

  def next: Unit = 1.next
  def n: Unit = next
  def > : Unit = next

  def previous: Unit = 1.previous
  def p: Unit = previous
  def < : Unit = previous

  def first: Unit = 1.go
  def f: Unit = first
  def << : Unit = first

  def last: Unit = deck.size.go
  def l: Unit = last
  def >> : Unit = last

  def blank: Unit = print(config.newline * config.screenHeight)
  def b: Unit = blank

  def help: Unit = print(helpMessage)
  def h: Unit = help
  def ? : Unit = help
}
