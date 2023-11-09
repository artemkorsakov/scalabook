package ru.scalabook.typeclass.monoid

import cats.data.*
import cats.syntax.apply.*

trait SemiringLaw extends CMonoidLaw, MultiplicativeSemigroupLaw:
  def checkSemiringLaw[A: Semiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    (checkCMonoidLaw(x, y, z), checkMultiplicativeSemigroupLaw(x, y, z)).mapN {
      case (_, _) => ()
    }
