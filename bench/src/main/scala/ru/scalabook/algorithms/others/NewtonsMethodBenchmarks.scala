package ru.scalabook.algorithms.others

import ru.scalabook.algorithms.others.NewtonsMethod.newtonsMethod

private val g: Double => Double = x => math.cos(x) - x * x * x

@main def newtonsMethodBench(): Unit =
  println(newtonsMethod(g, 0.5))
