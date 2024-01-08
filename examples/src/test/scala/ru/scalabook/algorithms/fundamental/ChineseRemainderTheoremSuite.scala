package ru.scalabook.algorithms.fundamental

import ru.scalabook.algorithms.fundamental.ChineseRemainderTheorem.*
import spire.math.SafeLong
import weaver.FunSuite

object ChineseRemainderTheoremSuite extends FunSuite:
  test("Chinese remainder theorem"):
    exists(solution(Array(2, 3), Array(1, 2)))(n => expect(n == 5)) and
      exists(solution(Array(707, 527), Array(0, 5)))(n =>
        expect(n == 258762)
      ) and
      exists(solution(
        Array(1504170715041707L, 4503599627370517L),
        Array(0L, 8912517754604L)
      ))(n => expect(n == SafeLong(1504170715041707L * 3)))
