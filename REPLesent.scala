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
, source: String = "REPLesent.txt"
, slideCounter: Boolean = false
, slideTotal: Boolean = false
, intp: scala.tools.nsc.interpreter.IMain = null
) {
  import java.io.File
  import scala.util.matching.Regex
  import scala.util.{ Try, Success, Failure }

  private case class Config(
    top: String = "*"
  , bottom: String = "*"
  , sinistral: String = "* "
  , dextral: String = " *"
  , newline: String = System.lineSeparator
  , whiteSpace: String = " "
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

          stty.trim.split(' ') map (_.toInt)
        } getOrElse Array(0, 0)

        val screenWidth = Seq(width, w) find (_ > 0) getOrElse defaultWidth
        val screenHeight = Seq(height, h) find (_ > 0) getOrElse defaultHeight

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

    val blankLine = {
      val padding = if (dextral.isEmpty) "" else whiteSpace * horizontalSpace + dextral
      sinistral + padding + newline
    }
  }

  private val config = Config(width = width, height = height)

  private case class Line(content: String, length: Int, private val style: Line.Style) {
    override def toString: String = content
    def isEmpty: Boolean = content.isEmpty
    def render(margin: Int): String = style(this, margin)
  }

  private object Line {
    import scala.io.AnsiColor._

    protected sealed trait Style {
      import config.whiteSpace

      protected def horizontalSpace = config.horizontalSpace

      protected def fill(line: Line, left: Int, right: Int): String = {
        whiteSpace * left + line + whiteSpace * right
      }

      def apply(line: Line, margin: Int): String
    }

    private object HorizontalRuler extends Style {
      private val ansiBegin = RESET.head
      private val ansiEnd = RESET.last

      private val defaultPattern = Line("-")

      def apply(line: Line, margin: Int): String = {
        // Provides a default pattern if none was specified
        val pattern = if (line.isEmpty) defaultPattern else line

        val width = horizontalSpace - margin
        val repeats = width / pattern.length

        val content = pattern.toString * repeats

        var remaining = width - repeats * pattern.length
        var ansi = false
        var reset = ""

        val padding = pattern.toString takeWhile { c =>
          val continue = remaining > 0

          if (continue) c match {
            case `ansiEnd` if ansi => ansi = false
            case _ if ansi => // no-op
            case `ansiBegin` => ansi = true; reset = RESET
            case c if Character.isHighSurrogate(c) => // no-op
            case _ => remaining -= 1
          }

          continue
        }

        val left = margin / 2
        val right = margin - left

        val l = Line(content + padding + reset, width, LeftAligned)

        fill(l, left, right)
      }
    }

    private object FullScreenHorizontalRuler extends Style {
      def apply(line: Line, ignored: Int): String = HorizontalRuler(line, 0)
    }

    private object LeftFlushed extends Style {
      def apply(line: Line, ignored: Int): String = {
        val left = 0
        val right = horizontalSpace - line.length

        fill(line, left, right)
      }
    }

    private object LeftAligned extends Style {
      def apply(line: Line, margin: Int): String = {
        val left = margin / 2
        val right = horizontalSpace - left - line.length

        fill(line, left, right)
      }
    }

    private object Centered extends Style {
      def apply(line: Line, ignored: Int): String = {
        val margin = horizontalSpace - line.length

        val left = margin / 2
        val right = margin - left

        fill(line, left, right)
      }
    }

    private object RightAligned extends Style {
      def apply(line: Line, margin: Int): String = {
        val right = (margin + 1) / 2
        val left = horizontalSpace - right - line.length

        fill(line, left, right)
      }
    }

    private object RightFlushed extends Style {
      def apply(line: Line, ignored: Int): String = {
        val left = horizontalSpace - line.length
        val right = 0

        fill(line, left, right)
      }
    }

    private def style(line: String): (String, Style) = line match {
      case s if s startsWith "<< " => (s.drop(3), LeftFlushed)
      case s if s startsWith "< " => (s.drop(2), LeftAligned)
      case s if s startsWith "| " => (s.drop(2), Centered)
      case s if s startsWith "> " => (s.drop(2), RightAligned)
      case s if s startsWith ">> " => (s.drop(3), RightFlushed)
      case s if s startsWith "//" => (s.drop(2), FullScreenHorizontalRuler)
      case s if s startsWith "/" => (s.drop(1), HorizontalRuler)
      case s: String => (s, LeftAligned)
    }

    private val ansiEscape = """\\.""".r

    private val ansiColor = Map(
      'b' -> BLUE,
      'c' -> CYAN,
      'g' -> GREEN,
      'k' -> BLACK,
      'm' -> MAGENTA,
      'r' -> RED,
      'w' -> WHITE,
      'y' -> YELLOW,
      'B' -> BLUE_B,
      'C' -> CYAN_B,
      'G' -> GREEN_B,
      'K' -> BLACK_B,
      'M' -> MAGENTA_B,
      'R' -> RED_B,
      'W' -> WHITE_B,
      'Y' -> YELLOW_B,
      '!' -> REVERSED,
      '*' -> BOLD,
      '_' -> UNDERLINED
    )

    private def ansi(line: String): (String, Int) = {
      var drop = 0
      var reset = ""

      val content: String = ansiEscape.replaceAllIn(line, m =>
        m.matched(1) match {
          case c if ansiColor.contains(c) => drop += 2; reset = RESET; ansiColor(c)
          case 's' => drop += 2; RESET
          case '\\' => drop += 1; "\\\\"
          case c: Char => "\\\\" + c
        }
      )

      (content + reset, drop)
    }

    private val emojiEscape = """:([\w+\-]+):""".r

    private lazy val emojis: Map[String, String] = {
      Try {
        val emoji = io.Source.fromFile("emoji.txt").getLines
        emoji.map { l =>
          val a = l.split(' ')
          (a(1), a(0))
        }.toMap
      } getOrElse Map.empty
    }

    private def emoji(line: String): (String, Int) = {
      var drop = 0

      val content: String = emojiEscape.replaceAllIn(line, m => {
        m.group(1) match {
          case e if emojis.contains(e) => drop += m.matched.length - 1; emojis(e)
          case _ => m.matched
        }
      })

      (content, drop)
    }

    def apply(line: String): Line = {
      val (l1, lineStyle) = style(line)
      val (l2, ansiDrop) = ansi(l1)
      val (content, emojiDrop) = emoji(l2)

      val length = l1.codePointCount(0, l1.length) - ansiDrop - emojiDrop

      Line(content = content, length = length, style = lineStyle)
    }
  }

  // `size` and `maxLength` refer to the dimensions of the slide's last build
  private case class Build(content: IndexedSeq[Line], size: Int, maxLength: Int, footer: Line)

  private case class Slide(content: IndexedSeq[Line], builds: IndexedSeq[Int], code: IndexedSeq[String]) {
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
      // "Stops" the cursor one position after/before the last/first slide to avoid
      // multiple next/previous calls taking it indefinitely away from the deck
      slideCursor = slide.min(slides.size).max(-1)

      buildCursor = build

      if (currentSlideIsDefined && currentSlide.hasBuild(buildCursor)) {
        Some(currentSlide.build(buildCursor, footer))
      } else None
    }

    def jumpTo(n: Int): Option[Build] = select(slide = n)

    def jump(n: Int): Option[Build] = jumpTo(slideCursor + n)

    def nextBuild: Option[Build] = select(build = buildCursor + 1) orElse jump(1)

    def redrawBuild: Option[Build] = select(build = buildCursor)

    def previousBuild: Option[Build] = select(build = buildCursor - 1) orElse {
      jump(-1) flatMap { _ =>
        select(build = currentSlide.lastBuild)
      }
    }

    def lastSlide: Option[Build] = jumpTo(slides.size - 1)

    def lastBuild: Option[Build] = jumpTo(slides.size) orElse previousBuild

    def currentSlideNumber: Int = slideCursor

    def runCode: Unit = {
      val code = currentSlide.code(buildCursor)

      if (repl.isEmpty) {
        Console.err.print(s"No reference to REPL found. Please call with parameter intp=$$intp")
      } else if (code.isEmpty) {
        Console.err.print("No code for you")
      } else {
        repl foreach (_.interpret(code))
      }
    }
  }

  private val helpMessage = """Usage:
    |  next          n      >     go to next build/slide
    |  previous      p      <     go back to previous build/slide
    |  redraw        z            redraw the current build/slide
    |  Next          N      >>    go to next slide
    |  Previous      P      <<    go back to previous slide
    |  i next        i n          advance i slides
    |  i previous    i p          go back i slides
    |  i go          i g          go to slide i
    |  first         f      |<    go to first slide
    |  last          l      >|    go to last slide
    |  Last          L      >>|   go to last build of last slide
    |  run           r      !!    execute code that appears on slide
    |  blank         b            blank screen
    |  help          h      ?     print this help message""".stripMargin

  private val repl = Option(intp)

  private var deck = Deck(parseSource(source))

  private def parseSource(path: String): IndexedSeq[Slide] = {
    Try {
      val pathFile = new File(path)
      val lines: Iterator[String] = (
        if (pathFile.isDirectory) {
          pathFile
            .list
            .sorted
            .filter(_.endsWith(".replesent"))
            .flatMap { name => io.Source.fromFile(new File(pathFile, name)).getLines }
            .toIterator
        } else {
          io.Source.fromFile(path).getLines
        }
      )
      parse(lines)
    } match {
      case Failure(e) =>
        e.printStackTrace
        Console.err.print(s"Sorry, could not parse $path. Quick, say something funny before anyone notices!")
        IndexedSeq.empty
      case Success(value) => value
    }
  }

  private def parse(lines: Iterator[String]): IndexedSeq[Slide] = {
    sealed trait LineHandler {
      def switch: LineHandler
      def apply(line: String): (Line, Option[String])
    }

    object LineHandler extends LineHandler {
      def switch: LineHandler = CodeHandler
      def apply(line: String): (Line, Option[String]) = (Line(line), None)
    }

    object CodeHandler extends LineHandler {
      private val patterns: Seq[(String, Regex)] = {
        val number: Regex = {
          val hex = "(?:0[xX][0-9A-Fa-f]+)"
          val decimal = "(?:[1-9][0-9]*|0)"
          val long = s"(?:${decimal}[DFLdfl])"
          val float = s"(?:${decimal}\\.${decimal}[DFdf])"
          val eNotation = s"(?:${decimal}(?:\\.0?${decimal})?[eE][+\\-]?[0-9]+)"
          s"""\\b(?:${eNotation}|${hex}|${long}|${float}|${decimal})\\b""".r
        }

        val string: Regex = "(?:s?\"(?:\\\\\"|[^\"])*\")".r
        val reserved: Regex = (
          s"""\\b(?:null|contains|exists|filter|filterNot|find|flatMap|""" +
          s"""flatten|fold|forall|foreach|getOrElse|map|orElse)\\b"""
        ).r
        val special: Regex = s"""\\b(?:true|false|this)\\b""".r
        val typeSig: Regex = {
          val token: String => String = { limit => s"[$$_]${limit}[A-Z][_$$A-Z0-9]${limit}[\\w$$]${limit}" }
          val prefix: String = s"""(?<=(?::)\\s{0,10}|\\btype ${token("{0,10}")}\\s{0,10}=\\s{0,10})"""
          s"""\\b(?:${prefix}(?:${token("*")}|\\s*=>\\s*|\\s*with\\s*)*)\\b"""
        }.r

        val syntax: Regex = (
          s"""\\b(?:abstract|case|catch|class|def|do|else|extends|final|""" +
          s"""finally|for|forSome|if|implicit|import|lazy|match|new|""" +
          s"""object|override|package|private|protected|return|sealed|""" +
          s"""super|throw|trait|try|type|val|var|while|with|yield)\\b"""
        ).r

        Seq[(String, Regex)](
          "r" -> string
        , "c" -> reserved
        , "m" -> special
        , "g" -> typeSig
        , "r" -> number
        , "y" -> syntax
        )
      }

      def switch: LineHandler = LineHandler

      def apply(line: String): (Line, Option[String]) = {
        val (colors, regexes) = patterns.unzip

        // new Regex("(?:(a)|(b)|(c))") will produce
        // m.subgroups List(null, "b", null) when applied on "b"
        val regex = new Regex(s"(?:(${regexes.mkString(")|(")}))")

        val formatted = regex.replaceAllIn(line, { m =>
          val colorIdx = m.subgroups.indexWhere(_ != null)
          colors.drop(colorIdx).take(1).headOption
            .map({ color =>
              s"\\\\${color}${Regex.quoteReplacement(m.toString)}\\\\s"
            })
            .getOrElse(line)
        })

        (Line("< " + formatted), Option(line))
      }
    }

    case class Acc(
      content: IndexedSeq[Line] = IndexedSeq.empty
    , builds: IndexedSeq[Int] = IndexedSeq.empty
    , deck: IndexedSeq[Slide] = IndexedSeq.empty
    , code: IndexedSeq[String] = IndexedSeq.empty
    , codeAcc: IndexedSeq[String] = IndexedSeq.empty
    , handler: LineHandler = LineHandler
    ) {
      import config.newline

      def switchHandler: Acc = copy(handler = handler.switch)

      def append(line: String): Acc = {
        val (l, c) = handler(line)
        copy(content = content :+ l, codeAcc = c.fold(codeAcc)(codeAcc :+ _))
      }

      def pushBuild: Acc = copy(
        builds = builds :+ content.size
      , code = code :+ codeAcc.mkString(newline)
      , codeAcc = IndexedSeq.empty
      )

      def pushSlide: Acc = {
        if (content.isEmpty) {
          append("").pushSlide
        } else {
          val finalBuild = pushBuild
          val slide = Slide(content, finalBuild.builds, finalBuild.code)

          Acc(deck = deck :+ slide)
        }
      }
    }

    val slideSeparator = "---"
    val buildSeparator = "--"
    val codeDelimiter = "```"

    val acc = lines.foldLeft(Acc()) { (acc, line) =>
      line match {
        case `slideSeparator` => acc.pushSlide
        case `buildSeparator` => acc.pushBuild
        case `codeDelimiter` => acc.switchHandler
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

    def render(line: Line): StringBuilder = {
      sb ++= sinistral
      sb ++= line.render(margin)
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

    build foreach { b =>
      print(render(b))
    }
  }
  private def reloadDeck(): Unit = {
    val curSlide = deck.currentSlideNumber
    deck = Deck(parseSource(source))
    show(deck.jumpTo(curSlide))
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

  def redraw: Unit = show(deck.redrawBuild)
  def z: Unit = redraw

  def reload: Unit = reloadDeck
  def y: Unit = reload

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

  def run: Unit = deck.runCode
  def r: Unit = run
  def !! : Unit = run

  def blank: Unit = print(config.newline * config.screenHeight)
  def b: Unit = blank

  def help: Unit = print(helpMessage)
  def h: Unit = help
  def ? : Unit = help
}
