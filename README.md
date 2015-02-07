REPLesent
=========

[![Build Status](https://travis-ci.org/marconilanna/REPLesent.svg)](https://travis-ci.org/marconilanna/REPLesent)
[![Codacy Badge](https://www.codacy.com/project/badge/2ec4acaf2e424baab3d0cb53d20d9df9)](https://www.codacy.com/public/marconilanna/REPLesent)

> **represent** *verb*
>  0. depict, portray, render, delineate, show, illustrate.
>  0. symbolize, stand for, personify, typify, embody.
>  0. point out, state, present, put forward.

REPLesent is a neat little tool to build presentations using the Scala REPL.

Conceptualized and originally implemented while waiting almost two hours for a plane
to be de-iced, it was introduced during the Northeast Scala Symposium 2015 in Boston.

While clearly not a Powerpoint replacement, REPLesent is a good alternative for
training sessions and technical talks that have a live coding component.
Its old-school looks was considered very cool by many conference attendants.

Getting Started
---------------

REPLesent is distributed as a single `.scala` file and has no dependencies.

REPLesent was designed to be used only in conjunction with the Scala REPL.
It is not meant to be compiled as a standalone application
(`build.sbt` is only for running the unit tests).

We recommend you install and use the full Scala distribution
(the `scala` command) instead of just the `sbt` console.
Scala 2.11.4 or later and JDK 7 or later are the preferred versions.

To get started, download and save to the same folder the files
`REPLesent.scala` and `REPLesent.txt`.

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

scala> val replesent = REPLesent(80, 25)
replesent: REPLesent = REPLesent(80,25,REPLesent.txt)

scala> import replesent._
import replesent._
```

Do not forget to replace `80, 25` with the actual size of your terminal window.
Once all is done, type `f`, press `ENTER`, and follow the on-screen instructions.

**Experimental screen size auto-detection:**
For Unix-like systems, including Mac OS X and Linux, you can omit the screen size.
Does not work for Microsoft Windows systems.

> *I don't care if it is used, I just want it to be useful*

License
-------

Copyright 2015 Marconi Lanna

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
