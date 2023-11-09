package ru.scalabook.typeclass.monoid

import cats.data.*

trait CSemiringLaw extends SemiringLaw:
  def checkCSemiringLaw[A: CSemiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemiringLaw(x, y, z) combine
      check(
        CSemiring[A].times(x, y) == CSemiring[A].times(y, x),
        "commutative: x * y = y * x"
      )
