package ru.scalabook.typeclass.monoid

trait CRingLaw extends RingLaw, munit.Assertions:
  def checkCRingLaw[A: CRing](x: A, y: A, z: A): Unit =
    checkRingLaw(x, y, z): Unit
    assertEquals(
      CRing[A].times(x, y),
      CRing[A].times(y, x),
      "times commutative"
    )
