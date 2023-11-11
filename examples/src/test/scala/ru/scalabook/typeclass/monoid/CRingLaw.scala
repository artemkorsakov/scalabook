package ru.scalabook.typeclass.monoid

import cats.data.ValidatedNel

trait CRingLaw extends RingLaw, munit.Assertions:
  def checkCRingLaw[A: CRing](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkRingLaw(x, y, z) combine
      check(
        CRing[A].times(x, y) == CRing[A].times(y, x),
        "times commutative: x * y = y * x"
      )
