package ru.scalabook.typeclass.monoid

import cats.data.*
import cats.syntax.apply.*

trait CSemigroupLaw extends SemigroupLaw:
  def checkCSemigroupLaw[A: CSemigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    (
      checkSemigroupLaw(x, y, z),
      check(
        CSemigroup[A].combine(x, y) == CSemigroup[A].combine(y, x),
        "commutative: x + y = y + x"
      )
    ).mapN { case (_, _) => () }
