/*
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/equations/QuadraticEquationBenchmarks.scala --main-class ru.scalabook.algorithms.equations.solutionsInIntegersBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/equations/QuadraticEquationBenchmarks.scala --main-class ru.scalabook.algorithms.equations.solutionsBench
scala-cli run bench/src/main/scala/ru/scalabook/algorithms/equations/QuadraticEquationBenchmarks.scala --main-class ru.scalabook.algorithms.equations.solutionsInComplexNumbersBench
 */

//> using dep nl.grons::metrics4-scala:4.3.2
//> using dep org.typelevel::spire:0.18.0
//> using file ../../../../../../../../examples/src/main/scala/ru/scalabook/algorithms/equations/QuadraticEquation.scala
//> using file ../../MetricInstrumented.scala
//> using file ../../Period.scala

package ru.scalabook.algorithms.equations

import ru.scalabook.MetricInstrumented
import ru.scalabook.Period
import spire.math.Complex
import QuadraticEquationBenchmarks.*

@main def solutionsInIntegersBench(): Unit =
  println(loadSolutionsInIntegers)
  prettyPrint(Period.MicroSecond)

@main def solutionsBench(): Unit =
  println(loadSolutions)
  prettyPrint(Period.MicroSecond)

@main def solutionsInComplexNumbersBench(): Unit =
  println(loadSolutionsInComplexNumbers)
  prettyPrint(Period.MicroSecond)

object QuadraticEquationBenchmarks extends MetricInstrumented:
  def loadSolutionsInIntegers: Array[Long] = loading.time:
    QuadraticEquation(1, -5, 6).solutionsInIntegers

  def loadSolutions: Array[Double] = loading.time:
    QuadraticEquation(3, -4, 1).solutions

  def loadSolutionsInComplexNumbers: Array[Complex[Double]] = loading.time:
    QuadraticEquation(1, -2, 2).solutionsInComplexNumbers
