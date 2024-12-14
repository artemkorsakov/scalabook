package ru.scalabook.typeclass.monoid

import cats.data.*
import ru.scalabook.typeclass.common.OldLaws

trait BandLaw extends OldLaws:
  def checkBandLaw[A: Band](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z) combine
      check(Band[A].combine(x, x) == x, "idempotency: x + x = x")
