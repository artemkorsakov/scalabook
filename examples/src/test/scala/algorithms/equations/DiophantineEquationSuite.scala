package algorithms.equations

import munit.FunSuite
import algorithms.equations.DiophantineEquation

class DiophantineEquationSuite extends FunSuite:
  test("minimalEquation and isSuccess") {
    assertEquals(
      DiophantineEquation(67).minimalEquation,
      Some((BigInt(48842), BigInt(5967)))
    )
    assertEquals(
      DiophantineEquation(13).minimalEquation,
      Some((BigInt(649), BigInt(180)))
    )
    assertEquals(
      DiophantineEquation(2).minimalEquation,
      Some((BigInt(3), BigInt(2)))
    )
    assertEquals(
      DiophantineEquation(3).minimalEquation,
      Some((BigInt(2), BigInt(1)))
    )
    assertEquals(
      DiophantineEquation(5).minimalEquation,
      Some((BigInt(9), BigInt(4)))
    )
    assertEquals(
      DiophantineEquation(6).minimalEquation,
      Some((BigInt(5), BigInt(2)))
    )
    assertEquals(
      DiophantineEquation(7).minimalEquation,
      Some((BigInt(8), BigInt(3)))
    )
    assertEquals(DiophantineEquation(4).minimalEquation, None)
    assertEquals(DiophantineEquation(9).minimalEquation, None)
    assertEquals(
      DiophantineEquation(61).minimalEquation,
      Some((BigInt(1766319049), BigInt(226153980)))
    )
    assertEquals(
      DiophantineEquation(73).minimalEquation,
      Some((BigInt(2281249), BigInt(267000)))
    )
    assertEquals(
      DiophantineEquation(94).minimalEquation,
      Some((BigInt(2143295), BigInt(221064)))
    )
    assertEquals(
      DiophantineEquation(109).minimalEquation,
      Some((BigInt(158070671986249L), BigInt(15140424455100L)))
    )
    assertEquals(
      DiophantineEquation(110).minimalEquation,
      Some((BigInt(21), BigInt(2)))
    )
    assertEquals(
      DiophantineEquation(61).isSuccess(BigInt(1766319049), BigInt(226153980)),
      true
    )
  }
