package ru.scalabook.typeclass.monoid

import cats.data.*
import cats.syntax.apply.*

trait CMonoidLaw extends MonoidLaw, CSemigroupLaw:
  def checkCMonoidLaw[A: CMonoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    (checkMonoidLaw(x, y, z), checkCSemigroupLaw(x, y, z)).mapN { case (_, _) =>
      ()
    }
