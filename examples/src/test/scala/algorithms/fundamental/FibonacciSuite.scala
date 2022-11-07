package algorithms.fundamental

import algorithms.fundamental.Fibonacci.memoizedFib
import munit.FunSuite

class FibonacciSuite extends FunSuite:
  test("Fibonacci should memorize result") {
    assertEquals(memoizedFib(30), BigInt(832040))
    assertEquals(memoizedFib(40), BigInt(102334155))
    assertEquals(memoizedFib(45), BigInt(1134903170))
    assertEquals(memoizedFib(60), BigInt(1548008755920L))
  }
