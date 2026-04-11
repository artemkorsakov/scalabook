/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/equations/PolynomialEquationBenchmarks.scala
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/equations/PolynomialEquation.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.equations

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import ru.scalabook.algorithms.equations.PolynomialEquation.*

import PolynomialEquationBenchmarks.*

@main def polynomialCoefficientsBench(): Unit =
  println(loadStuff)
  prettyPrint(Period.Second)

object PolynomialEquationBenchmarks extends MetricInstrumented:
  def loadStuff: Array[Long] = loading.time:
    polynomialCoefficients(3, Array(1, 683, 44287))
