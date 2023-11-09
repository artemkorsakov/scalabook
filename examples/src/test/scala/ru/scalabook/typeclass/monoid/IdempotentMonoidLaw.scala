package ru.scalabook.typeclass.monoid

import cats.data.*

trait IdempotentMonoidLaw extends MonoidLaw, BandLaw:
  def checkIdempotentMonoidLaw[A: IdempotentMonoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkMonoidLaw(x, y, z) combine checkBandLaw(x, y, z)
