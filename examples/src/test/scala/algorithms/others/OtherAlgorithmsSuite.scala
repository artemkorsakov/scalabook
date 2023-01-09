package algorithms.others

import munit.FunSuite
import algorithms.others.OtherAlgorithms.*

class OtherAlgorithmsSuite extends FunSuite:
  test("Функция Аккермана - проверка на небольших значениях") {
    assertEquals(functionAckermann(1, 10), 1024)
    assertEquals(functionAckermann(2, 4), 65536)
    assertEquals(functionAckermann(3, 3), 65536)
  }
