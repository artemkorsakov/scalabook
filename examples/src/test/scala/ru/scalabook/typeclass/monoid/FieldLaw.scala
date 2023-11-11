package ru.scalabook.typeclass.monoid

import cats.data.ValidatedNel

trait FieldLaw extends EuclideanRingLaw:
  def checkFieldLaw[A: Field](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkEuclideanRingLaw(x, y, z) combine
      check(
        x == Field[A].empty || Field[A].times(x, x.reciprocal) == Field[A].one,
        "x * x^{-1} = 1"
      )
