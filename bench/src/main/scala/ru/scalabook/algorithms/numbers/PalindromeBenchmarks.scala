/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/numbers/PalindromeBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/numbers/Palindrome.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.Palindrome.isPalindrome
import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import PalindromeBenchmarks.*

@main def isPalindromeBench(): Unit =
  loadStuff()
  prettyPrint(Period.MicroSecond)

object PalindromeBenchmarks extends MetricInstrumented:
  def loadStuff(): Unit = loading.time:
    println((1 to 1000000).count(isPalindrome))
