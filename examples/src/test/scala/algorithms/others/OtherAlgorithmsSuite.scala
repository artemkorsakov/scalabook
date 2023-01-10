package algorithms.others

import munit.FunSuite
import algorithms.others.OtherAlgorithms.*

class OtherAlgorithmsSuite extends FunSuite:
  test("Вычисление факториала") {
    assertEquals(factorial(0), BigInt(1))
    assertEquals(factorial(1), BigInt(1))
    assertEquals(factorial(2), BigInt(2))
    assertEquals(factorial(9), BigInt(362880))
    assertEquals(factorial(15), BigInt("1307674368000"))
    assertEquals(factorial(30), BigInt("265252859812191058636308480000000"))
  }

  test("Функция Аккермана - проверка на небольших значениях") {
    assertEquals(functionAckermann(1, 10), 1024)
    assertEquals(functionAckermann(2, 4), 65536)
    assertEquals(functionAckermann(3, 3), 65536)
  }
