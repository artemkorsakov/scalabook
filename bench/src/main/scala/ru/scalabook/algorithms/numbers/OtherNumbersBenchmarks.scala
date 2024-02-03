package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.OtherNumbers.*
import spire.math.SafeLong

@main def bernoulliBench(): Unit =
  println(bernoulli(500))

@main def padovanBench(): SafeLong =
  padovan(1_000_000)

@main def jacobsthalBench(): SafeLong =
  jacobsthal(1_000_000)

@main def pellBench(): SafeLong =
  pell(1_000_000)

@main def tribonacciBench(): SafeLong =
  tribonacci(1_000_000)

@main def tetranacciBench(): SafeLong =
  tetranacci(1_000_000)
