package algorithms.numbers

import munit.FunSuite
import algorithms.numbers.ComplexNumber

class ComplexNumberSuite extends FunSuite:
  test("add, sub, mul") {
    assertEquals(
      ComplexNumber(2, 3).add(ComplexNumber(1, 4)),
      ComplexNumber(3.0, 7.0)
    )
    assertEquals(
      ComplexNumber(2, 3) + ComplexNumber(1, 4),
      ComplexNumber(3.0, 7.0)
    )
    assertEquals(
      ComplexNumber(2, 3).sub(ComplexNumber(1, 4)),
      ComplexNumber(1.0, -1.0)
    )
    assertEquals(
      ComplexNumber(2, 3) - ComplexNumber(1, 4),
      ComplexNumber(1.0, -1.0)
    )
    assertEquals(
      ComplexNumber(2, 3).mul(ComplexNumber(1, 4)),
      ComplexNumber(-10.0, 11.0)
    )
    assertEquals(
      ComplexNumber(2, 3) * ComplexNumber(1, 4),
      ComplexNumber(-10.0, 11.0)
    )
  }

  test("div") {
    assertEqualsComplexNumber(
      ComplexNumber(2, 3).div(ComplexNumber(1, 4)),
      ComplexNumber(14.0 / 17.0, -5.0 / 17.0)
    )
    assertEqualsComplexNumber(
      ComplexNumber(2, 3) / ComplexNumber(1, 4),
      ComplexNumber(14.0 / 17.0, -5.0 / 17.0)
    )
    assertEqualsComplexNumber(
      (ComplexNumber(1, 1) * ComplexNumber(2, 1) * ComplexNumber(
        3,
        1
      )) / ComplexNumber(1, -1),
      ComplexNumber(-5.0, 5.0)
    )
    assertEqualsComplexNumber(
      ComplexNumber(1, 1) / ComplexNumber(1, -1),
      ComplexNumber(0.0, 1.0)
    )
    assertEqualsComplexNumber(
      ComplexNumber(1, 1) / ComplexNumber(2, -1),
      ComplexNumber(1.0 / 5.0, 3.0 / 5.0)
    )
    assertEqualsComplexNumber(
      ComplexNumber(1, 0) / ComplexNumber(0, 1),
      ComplexNumber(0.0, -1.0)
    )
    assertEqualsComplexNumber(
      (ComplexNumber(1, 0) / ComplexNumber(-2, 1)) / ComplexNumber(1, -3),
      ComplexNumber(1.0 / 50.0, -7.0 / 50.0)
    )
  }

  test("abs") {
    assertEquals(ComplexNumber(5, 6).abs, ComplexNumber(5.0, 6.0))
    assertEquals(ComplexNumber(-5, 6).abs, ComplexNumber(5.0, 6.0))
    assertEquals(ComplexNumber(5, -6).abs, ComplexNumber(5.0, 6.0))
    assertEquals(ComplexNumber(-5, -6).abs, ComplexNumber(5.0, 6.0))
  }

  test("conjugate") {
    assertEquals(ComplexNumber(5, 6).conjugate, ComplexNumber(5.0, -6.0))
    assertEquals(
      ComplexNumber(5, 6).conjugate.conjugate,
      ComplexNumber(5.0, 6.0)
    )
  }

  test("productWithConjugate") {
    assertEquals(ComplexNumber(5.0, 12.0).productWithConjugate, 169.0)
  }

  test("sqrt") {
    val roots = ComplexNumber(5.0, 12.0).sqrt
    assertEqualsComplexNumber(roots.head, ComplexNumber(3.0, 2.0))
    assertEqualsComplexNumber(roots.last, ComplexNumber(3.0, -2.0))
  }

  test("power2") {
    assertEqualsComplexNumber(
      ComplexNumber(5.0, 3.0).power2,
      ComplexNumber(16.0, 30.0)
    )
    assertEqualsComplexNumber(
      ComplexNumber(5.0, 3.0).power2.power2,
      ComplexNumber(-644.0, 960.0)
    )
    assertEqualsComplexNumber(
      ComplexNumber(5.0, 3.0).power2.power2.power2,
      ComplexNumber(-506864.0, -1236480.0)
    )
    assertEqualsComplexNumber(
      ComplexNumber(-0.5, Math.sqrt(3.0) / 2.0).power2,
      ComplexNumber(-0.5, -math.sqrt(3.0) / 2.0)
    )

  }

  test("power") {
    assertEqualsComplexNumber(
      ComplexNumber(5.0, 3.0).power(8),
      ComplexNumber(-506864.0, -1236480.0)
    )
    assertEqualsComplexNumber(
      ComplexNumber(-1.0 / 2.0, Math.sqrt(3.0) / 2.0).power(3),
      ComplexNumber(1.0, 0.0)
    )
  }

  private def assertEqualsComplexNumber(
      actual: ComplexNumber,
      expected: ComplexNumber
  ): Unit =
    assertEqualsDouble(actual.a, expected.a, 0.01)
    assertEqualsDouble(actual.b, expected.b, 0.01)
