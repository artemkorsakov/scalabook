package ru.scalabook.algorithms.others

import munit.FunSuite
import ru.scalabook.algorithms.others.Factorial.*

class FactorialSuite extends FunSuite:
  test("Вычисление факториала"):
    assertEquals(factorial(0), BigInt(1))
    assertEquals(factorial(1), BigInt(1))
    assertEquals(factorial(2), BigInt(2))
    assertEquals(factorial(9), BigInt(362880))
    assertEquals(factorial(15), BigInt("1307674368000"))
    assertEquals(factorial(30), BigInt("265252859812191058636308480000000"))

  test("Вычисление приближенного факториала по формуле Стирлинга"):
    assertEquals(approximateFactorial(8), BigInt(39902))
