package algorithms.equations

import munit.FunSuite
import algorithms.equations.PolynomialEquation.{
  polynomialCoefficients,
  polynomialSolution
}

class PolynomialEquationSuite extends FunSuite:
  private val goodPolynomialArrayTemp: Array[Long] =
    (1L to 10L).map(i => i * i * i).toArray
  private val aList0 =
    polynomialCoefficients(2, goodPolynomialArrayTemp)
  private val aList1 =
    polynomialCoefficients(3, goodPolynomialArrayTemp)
  private val expected = Array(1, 683, 44287, 838861, 8138021, 51828151,
    247165843, 954437177, 3138105961L, 9090909091L, 23775972551L, 57154490053L,
    128011456717L, 269971011311L, 540609741211L, 1034834473201L, 1903994239313L,
    3382547898907L, 5824512944911L)
  private val polCoeff1 = polynomialCoefficients(1, expected)
  private val polCoeff2 = polynomialCoefficients(2, expected)
  private val polCoeff3 = polynomialCoefficients(3, expected)
  private val polCoeff10 =
    polynomialCoefficients(10, expected)

  test("polynomialCoefficients") {
    assertEquals(polynomialSolution(3, aList0), 15L)
    assertEquals(polynomialSolution(2, aList0), 8L)
    assertEquals(polynomialSolution(2, aList1), 8L)
    assertEquals(polynomialSolution(1, aList1), 1L)
    assertEquals(polynomialSolution(1, aList0), 1L)
    assertEquals(polynomialSolution(3, aList1), 27L)
    assertEquals(polynomialSolution(4, aList1), 58L)
    assertEquals(polynomialSolution(1, polCoeff1), 1L)
    assertEquals(polynomialSolution(2, polCoeff1), 1L)
    assertEquals(polynomialSolution(1, polCoeff2), 1L)
    assertEquals(polynomialSolution(2, polCoeff2), 683L)
    assertEquals(polynomialSolution(3, polCoeff2), 1365L)
    assertEquals(polynomialSolution(1, polCoeff3), 1L)
    assertEquals(polynomialSolution(2, polCoeff3), 683L)
    assertEquals(polynomialSolution(3, polCoeff3), 44287L)
    assertEquals(polynomialSolution(4, polCoeff3), 130813L)
    assertEquals(polynomialSolution(1, polCoeff10), 1L)
    assertEquals(polynomialSolution(2, polCoeff10), 683L)
    assertEquals(polynomialSolution(3, polCoeff10), 44287L)
    assertEquals(polynomialSolution(4, polCoeff10), 838861L)
    assertEquals(polynomialSolution(5, polCoeff10), 8138021L)
    assertEquals(polynomialSolution(6, polCoeff10), 51828151L)
    assertEquals(polynomialSolution(7, polCoeff10), 247165843L)
    assertEquals(polynomialSolution(8, polCoeff10), 954437177L)
    assertEquals(polynomialSolution(9, polCoeff10), 3138105961L)
    assertEquals(polynomialSolution(10, polCoeff10), 9090909091L)
    assertEquals(polynomialSolution(11, polCoeff10), 23772343751L)
  }
