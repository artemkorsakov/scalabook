package ru.scalabook.typeclass.monoid

import cats.data.*

trait ISemiringLaw extends SemiringLaw, IdempotentMonoidLaw:
  def checkISemiringLaw[A: ISemiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemiringLaw(x, y, z) combine
      checkIdempotentMonoidLaw(x, y, z)
