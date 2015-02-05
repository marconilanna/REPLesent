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
  )

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

  private val config = Config()

  private val slideSeparator = "---"
  private val fragmentSeparator = "--"

  private val newline = System.lineSeparator

  private val (screenWidth, screenHeight) = screenSize(width, height)

  private val deck: IndexedSeq[Slide] = parseFile(input)

  private var cursor = -1

  private def screenSize(width: Int, height: Int)  = {
    if (width > 0 && height > 0) (width, height) else {
      // Experimental support for screen size auto-detection.
      // Supports only Unix-like systems, including Mac OS X and Linux.
      // Does not work with Microsoft Windows.
      val Array(h, w) = Try {
        import scala.sys.process._

        val stty = Seq("sh", "-c", "stty size < /dev/tty").!!

        stty.trim.split(' ') map { _.toInt }
      } getOrElse Array(0, 0)

      val screenWidth = if (width > 0) width
        else if (w > 0) w
        else 80

      val screenHeight = if (height > 0) height
        else if (h > 0) h
        else 25

      (screenWidth, screenHeight)
    }
  }

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
      def append(line: String) = copy(content = content :+ line)

      def pushFragment = copy(fragments = fragments :+ content.size)

      def pushSlide = {
        val fragments = pushFragment.fragments
        val maxLength = content.maxBy(_.length).length
        val slides = fragments map { n =>
          Slide(content.take(n), size = content.size, maxLength = maxLength)
        }

        Acc(deck = deck ++ slides)
      }
    }

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

    def fill(s: String) = if (s.isEmpty) s else {
      val t = s * (screenWidth / s.length)
      t + s.take(screenWidth - t.length)
    }

    val verticalSpace = screenHeight - 3 // accounts for header, footer, and REPL prompt
    val horizontalSpace = screenWidth - sinistral.length - dextral.length

    val topPadding = (verticalSpace - slide.size) / 2
    val bottomPadding = verticalSpace - topPadding - slide.content.size

    val leftPadding = " " * ((horizontalSpace - slide.maxLength) / 2)

    val blank = sinistral + {
      if (dextral.isEmpty) newline
      else (" " * horizontalSpace) + dextral + newline
    }

    val sb = StringBuilder.newBuilder

    sb ++= fill(top) + newline
    sb ++= blank * topPadding

    slide.content foreach { line =>
      sb ++= sinistral + leftPadding + line
      if (dextral.nonEmpty)
        sb ++= " " * (horizontalSpace - leftPadding.length - line.length) + dextral
      sb ++= newline
    }

    sb ++= blank * bottomPadding
    sb ++= fill(bottom)

    sb.mkString
  }

  private def display(n: Int) = {
    if (deck.isDefinedAt(n)) {
      val slide = render(deck(n))
      print(slide)
    } else {
      Console.err.print("No slide for you")
    }
  }

  private def jumpTo(n: Int) = {
    // "Stops" the cursor one position after/before the last/first slide to avoid
    // multiple next/previous calls taking it indefinitely away from the deck
    cursor = if (n > deck.size) deck.size
      else if (n < 0) -1
      else n
    display(cursor)
  }

  implicit class Ops(val i: Int) {
    def next = jumpTo(cursor + i)
    def n = next

    def previous = jumpTo(cursor - i)
    def p = previous

    def go = jumpTo(i - 1)
    def g = go
  }

  def next = 1.next
  def n = next
  def > = next

  def previous = 1.previous
  def p = previous
  def < = previous

  def first = 1.go
  def f = first
  def << = first

  def last = deck.size.go
  def l = last
  def >> = last

  def blank = print(newline * screenHeight)
  def b = blank

  def help = print(helpMessage)
  def h = help
  def ? = help
}
