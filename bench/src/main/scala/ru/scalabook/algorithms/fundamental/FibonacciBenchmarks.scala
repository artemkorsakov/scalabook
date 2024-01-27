package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Fibonacci.*

@main def memoizedFibBench(): BigInt =
  memoizedFib(2000)

@main def iterativeFibBench(): BigInt =
  iterativeFib(1_000_000)

@main def fastFibBench(): BigInt =
  fastFib(1_000_000)

@main def approximateFibonacciBench(): BigInt =
  approximateFibonacci(1_000_000)
