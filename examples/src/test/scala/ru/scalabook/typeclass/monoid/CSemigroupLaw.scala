package ru.scalabook.typeclass.monoid

trait CSemigroupLaw extends SemigroupLaw, munit.Assertions:
  def checkCSemigroupLaw[A: CSemigroup](x: A, y: A, z: A): Unit =
    checkSemigroupLaw(x, y, z)
    assertEquals(
      CSemigroup[A].combine(x, y),
      CSemigroup[A].combine(y, x),
      "commutative"
    )
