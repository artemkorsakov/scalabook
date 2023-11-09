package ru.scalabook.typeclass.monoid

import cats.data.*
import cats.syntax.apply.*

trait AbGroupLaw extends GroupLaw, CMonoidLaw:
  def checkAbGroupLaw[A: AbGroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    (checkGroupLaw(x, y, z), checkCMonoidLaw(x, y, z))
      .mapN { case (_, _) =>
        ()
      }
