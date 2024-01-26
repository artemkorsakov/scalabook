package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Primes.*

@main def primesBench(): Boolean =
  primes.take(100_000).length == 100_000

@main def isPrimeBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    isPrime(i)

@main def sieveOfEratosthenesBench(): Array[Boolean] =
  sieveOfEratosthenes(1_000_000)

@main def primeFactorsWithPowBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    primeFactorsWithPow(i)

@main def primeFactorsBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    primeFactors(i)

@main def nextPrimeBench(): Unit =
  (1 to 1_000_000).foreach: i =>
    nextPrime(i)
