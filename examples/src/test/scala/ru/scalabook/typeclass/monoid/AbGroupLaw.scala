package ru.scalabook.typeclass.monoid

import cats.data.*
import ru.scalabook.typeclass.common.OldLaws

trait AbGroupLaw extends OldLaws:
  def checkAbGroupLaw[A: AbGroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkGroupLaw(x, y, z) combine checkCMonoidLaw(x, y, z)
