package ru.scalabook.typeclass.monoid

import cats.data.*
import cats.syntax.apply.*

trait BandLaw extends SemigroupLaw:
  def checkBandLaw[A: Band](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    (
      checkSemigroupLaw(x, y, z),
      check(Band[A].combine(x, x) == x, "idempotency: x + x = x")
    ).mapN { case (_, _) => () }
