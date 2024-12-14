package ru.scalabook.typeclass.monoid

import cats.data.*
import ru.scalabook.typeclass.common.OldLaws

trait CMonoidLaw extends OldLaws, CSemigroupLaw:
  def checkCMonoidLaw[A: CMonoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkMonoidLaw(x, y, z) combine checkCSemigroupLaw(x, y, z)
