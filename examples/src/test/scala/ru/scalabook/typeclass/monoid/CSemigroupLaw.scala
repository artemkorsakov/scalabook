package ru.scalabook.typeclass.monoid

import cats.data.*
import ru.scalabook.typeclass.common.OldLaws

trait CSemigroupLaw extends OldLaws:
  def checkCSemigroupLaw[A: CSemigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z) combine
      check(
        CSemigroup[A].combine(x, y) == CSemigroup[A].combine(y, x),
        "commutative: x + y = y + x"
      )
