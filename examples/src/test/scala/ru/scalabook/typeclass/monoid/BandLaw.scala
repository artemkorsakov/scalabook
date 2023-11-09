package ru.scalabook.typeclass.monoid

import cats.data.*

trait BandLaw extends SemigroupLaw:
  def checkBandLaw[A: Band](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z) combine
      check(Band[A].combine(x, x) == x, "idempotency: x + x = x")
