package ru.scalabook.typeclass.monoid

import cats.data.ValidatedNel

trait RingLaw extends AbGroupLaw, SemiringLaw:
  def checkRingLaw[A: Ring](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkAbGroupLaw(x, y, z) combine
      checkSemiringLaw(x, y, z)
