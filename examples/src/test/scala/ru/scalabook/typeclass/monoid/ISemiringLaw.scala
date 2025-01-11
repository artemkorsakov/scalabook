package ru.scalabook.typeclass.monoid

import cats.data.*
import ru.scalabook.typeclass.common.OldLaws

trait ISemiringLaw extends SemiringLaw, OldLaws:
  def checkISemiringLaw[A: ISemiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemiringLaw(x, y, z) combine
      checkIdempotentMonoidLaw(x, y, z)
