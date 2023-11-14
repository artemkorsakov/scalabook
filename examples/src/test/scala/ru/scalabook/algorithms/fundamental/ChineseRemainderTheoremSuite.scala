package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.ChineseRemainderTheorem.*
import weaver.FunSuite

object ChineseRemainderTheoremSuite extends FunSuite:
  test("Chinese remainder theorem"):
    expect.all(
      solution(Array(2, 3), Array(1, 2)) == BigInt(5),
      solution(Array(707, 527), Array(0, 5)) == BigInt(258762),
      solution(
        Array(1504170715041707L, 4503599627370517L),
        Array(0L, 8912517754604L)
      ) == BigInt(1504170715041707L * 3)
    )
