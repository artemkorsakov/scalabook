package ru.scalabook.typeclass.monoid

import cats.data.*
import cats.syntax.apply.*

trait IdempotentMonoidLaw extends MonoidLaw, BandLaw:
  def checkIdempotentMonoidLaw[A: IdempotentMonoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    (checkMonoidLaw(x, y, z), checkBandLaw(x, y, z)).mapN { case (_, _) => () }
