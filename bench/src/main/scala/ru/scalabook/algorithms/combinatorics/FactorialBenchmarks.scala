package ru.scalabook.algorithms.combinatorics

import ru.scalabook.algorithms.combinatorics.Factorial.*
import ru.scalabook.algorithms.fundamental.Primes.primes
import spire.math.SafeLong

@main def factorialBench(): SafeLong =
  factorial(100000)

@main def approximateFactorialBench(): BigInt =
  approximateFactorial(100000)

@main def approximateFactorial2Bench(): BigInt =
  approximateFactorial2(100000)

@main def primeFactorBench(): Unit =
  val limit = 100_000
  primes.take(limit).toList.foreach: p =>
    primeFactor(limit, p)
