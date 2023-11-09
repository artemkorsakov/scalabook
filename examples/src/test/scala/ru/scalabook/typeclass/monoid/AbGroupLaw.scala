package ru.scalabook.typeclass.monoid

import cats.data.*

trait AbGroupLaw extends GroupLaw, CMonoidLaw:
  def checkAbGroupLaw[A: AbGroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkGroupLaw(x, y, z) combine checkCMonoidLaw(x, y, z)
