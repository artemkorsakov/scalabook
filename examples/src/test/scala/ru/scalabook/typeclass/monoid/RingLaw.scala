package ru.scalabook.typeclass.monoid

import cats.data.ValidatedNel
import ru.scalabook.typeclass.common.OldLaws

trait RingLaw extends OldLaws:
  def checkRingLaw[A: Ring](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkAbGroupLaw(x, y, z) combine
      checkSemiringLaw(x, y, z)
