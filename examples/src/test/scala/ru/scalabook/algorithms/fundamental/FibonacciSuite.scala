package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.Fibonacci.*
import weaver.FunSuite

object FibonacciSuite extends FunSuite:
  test("Calculating Fibonacci numbers using memorization"):
    expect.all(
      memoizedFib(30) == BigInt(832040),
      memoizedFib(40) == BigInt(102334155),
      memoizedFib(45) == BigInt(1134903170),
      memoizedFib(60) == BigInt(1548008755920L)
    )

  test("Iterative calculation of Fibonacci numbers"):
    expect.all(
      iterativeFib(30) == BigInt(832040),
      iterativeFib(40) == BigInt(102334155),
      iterativeFib(45) == BigInt(1134903170),
      iterativeFib(60) == BigInt(1548008755920L)
    )

  test("Calculating Fibonacci numbers with logarithmic complexity"):
    expect.all(
      fastFib(30) == BigInt(832040),
      fastFib(40) == BigInt(102334155),
      fastFib(45) == BigInt(1134903170),
      fastFib(60) == BigInt(1548008755920L)
    )

  test(
    "Calculating the approximate value of the Fibonacci number using the 'golden ratio'"
  ):
    expect.all(
      approximateFibonacci(30) == BigInt(832040),
      approximateFibonacci(40) == BigInt(102334155),
      approximateFibonacci(45) == BigInt(1134903170),
      approximateFibonacci(100) == BigInt("354224848179265190133")
    )
