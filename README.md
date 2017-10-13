REPLesent
=========

[![Build Status](https://travis-ci.org/marconilanna/REPLesent.svg)](https://travis-ci.org/marconilanna/REPLesent)
[![Codacy Badge](https://www.codacy.com/project/badge/2ec4acaf2e424baab3d0cb53d20d9df9)](https://www.codacy.com/public/marconilanna/REPLesent)

> **represent** *verb*
>  0. depict, portray, render, delineate, show, illustrate.
>  0. symbolize, stand for, personify, typify, embody.
>  0. point out, state, present, put forward.

REPLesent is a ~~neat little~~ powerful tool to build presentations using the Scala REPL.

Conceptualized and originally implemented while waiting almost two hours for my plane
to be de-iced, it was introduced during the Northeast Scala Symposium 2015 in Boston.

While clearly not a ~~Powerpoint~~ Keynote replacement, REPLesent is a good
option for training sessions and technical talks featuring live coding.
Its old-school looks were considered very cool by many conference attendants.

Features
--------

* Easy to write slides: a simple plain text file with minimal markup
* The full arsenal of navigation options: next, previous, first, last, jump to
* Builds (incremental slides)
* Slide number / total
* Text alignment: left, right, centered, flushed
* ANSI colors
* Horizontal rulers (thanks, [@daviscabral](https://github.com/daviscabral))
* Syntax highlighting
* Run code straight from slides directly in the REPL with a single keystroke.
No other presentation tool can do that for you!
* And the #1 requested feature: emoji! :-) I mean, :smile:

Quick Tour
----------

[![asciicast](https://asciinema.org/a/16690.png)](https://asciinema.org/a/16690)

Getting Started
---------------

REPLesent is distributed as a single `.scala` file and has no dependencies.

REPLesent was designed to be used only in conjunction with the Scala REPL.
It is not meant to be compiled as a standalone application
(`build.sbt` is only for running the unit tests).

We recommend you install and use the full Scala distribution
(the `scala` command) instead of just the `sbt` console.
Scala 2.11.4 or later and JDK 7 or later are the preferred versions. 
If you are using Windows, you need to run `scala` in
[ANSICON](https://github.com/adoxa/ansicon) or another console that
supports ANSI escape codes. REPLesent will not render correctly in
Windows' standard `cmd` console.

To get started, download and save to the same folder the files
`REPLesent.scala` and `REPLesent.txt`.
The file `emoji.txt` is optional, only needed to enable emoji support.

First, create an alias:

```sh
alias REPLesent='scala -Dscala.color -language:_ -nowarn -i REPLesent.scala'
```

Open the REPL and enter the two statements below:

```sh
$ REPLesent
Loading REPLesent.scala...
defined class REPLesent

Welcome to Scala version 2.11.5 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_31).
Type in expressions to have them evaluated.
Type :help for more information.

scala> val replesent = REPLesent(80, 25, intp=$intp)
replesent: REPLesent = REPLesent(80,25,REPLesent.txt)

scala> import replesent._
import replesent._
```

Do not forget to replace `80, 25` with the actual size of your terminal window.
The `intp=$intp` incantation is required to hook onto the REPL to run slide code.

Once all is done, type `f`, press `ENTER`, and follow the on-screen instructions.

**Experimental screen size auto-detection:**
For Unix-like systems, including Mac OS X and Linux, you can omit the screen size.
Does not work for Microsoft Windows systems.

Quick Reference Guide
---------------------

### Initialization options

Parameter | Type | Description | Default
--- | --- | --- | ---
`width` | `Int` | Terminal width in columns | `0` (Unix: autodetect, Windows: 80)
`height` | `Int` | Terminal height in rows | `0` (Unix: autodetect, Windows: 25)
`source` | `String` | The path to the presentation file | `"REPLesent.txt"`
`slideCounter` | `Boolean` | Whether to show the slide number | `false`
`slideTotal` | `Boolean` | Whether to show the total number of slides | `false`
`intp` | | A hook to the Scala REPL | No default, use magic value `$intp`

### Navigation commands

Command | Shortcut | Symbolic alias | Description
--- | --- | --- | ---
`next` | `n` | `>` | Go to next build/slide
`previous` | `p` | `<` | Go back to previous build/slide
`redraw` | `z` | | Redraw the current build/slide
`reload` | `y` | | Reload the slides from file
`Next` | `N` | `>>` | Go to next slide
`Previous` | `P` | `<<` | Go back to previous slide
`i next` | `i n` | | Advance i slides
`i previous` | `i p` | | Go back i slides
`i go` | `i g` | | Go to slide i
`first` | `f` | `|<` | Go to first slide
`last` | `l` | `>|` | Go to last slide
`Last` | `L` | `>>|` | Go to last build of last slide
`run` | `r` | `!!` | Execute code that appears on slide
`blank` | `b` | | Blank screen
`help` | `h` | `?` | This help message

### Separators and delimiters

Separator | Description
--- | ---
`---` | Separates slides
`--` | Separates builds
<code>```</code> | Delineates Scala code

### Text alignment

Command | Description
--- | ---
`<<` | Left-flushed text
`<` | Left-aligned text
`|` | Centered text
`>` | Right-aligned text
`>>` | Right-flushed text

A space separating the alignment command from the text is mandatory.

### ANSI colors

Escape code | Result
--- | ---
`\x` | Foreground color, where `x` is one of: `r`ed, `g`reen, `b`lue, `c`yan, `m`agenta, `y`ellow, blac`k`, `w`hite
`\X` | Background color, where capital `X` is one of the same as above
`\*` | Bold
`\_` | Underscore
`\!` | Reverse colors
`\s` | Resets to normal

### Horizontal rulers

Command | Description
--- | ---
`/` | A ruler across the slide length
`//` | A ruler across the entire screen width

An optional pattern may be specified immediately following the forward slash.
Unicode characters and ANSI color escapes (as above) are supported.

### Emoji

To enable emoji support, you will need a copy of the `emoji.txt` file.

Almost all shortcuts listed by the [Emoji cheat sheet](http://www.emoji-cheat-sheet.com/)
are supported.
You can look at `emoji.txt` for the definitive list of supported emoji.

Emojis can be combined with horizontal rulers and,
depending on your system fonts, ANSI colors, too.

Emoji has only be tested on Mac OS X Terminal.app and Gnome Terminal, YMMV.

### Multiple File Support

As your `REPLesent.txt` grows, you may find yourself needing to split slides into logical chunks for quick navigation.

In order to use this mode, simply place all your individual slide files into a directory (in this example, we'll be using the directory name `"slides"`), all ending with the file extension `.replesent`.

Then, initialize REPLesent with the additional parameter `source="slides"`, which will concatinate all files in ASCIIbetical order. Suggested practice is to name files with a leading sequence number, padding with zeros (01, 02, 03 ... 14, 15 ...)

One caveat is that since this is strict file concatination, slide separators will not be added automatically, so please remember to end your files with `---` or `--`.

### Vim Syntax Highlighting

Included in this repository is a `vim` directory, which contains a vim plugin for editing REPLesent slides.

This plugin depends on [derekwyatt/vim-scala](https://github.com/derekwyatt/vim-scala/) for Scala syntax highlighting.

Simply copy the files into your `~/.vim` directory to get started, or use [Vundle](https://github.com/VundleVim/Vundle.vim) (or similar) like:

    Plugin 'derekwyatt/vim-scala'
    Plugin 'marconilanna/REPLesent', {'rtp': 'vim/'}

Thanks
------

* [Davis Z. Cabral](https://github.com/daviscabral) for implementing
horizontal ruler support.
* [Noel Markham](https://github.com/noelmarkham) for the redraw command
* [Wojtek Pituła](https://github.com/Krever) for the reload command
* [Devon Stewart](https://github.com/blast-hardcheese/) for multi-file support, syntax highlighting plug-in for vim, and syntax highlighting enhancements.

License
-------

Copyright 2015-2017 Marconi Lanna

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
