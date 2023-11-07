package ru.scalabook.typeclass.monoid

import cats.data.*
import cats.syntax.apply.*

trait GroupLaw extends MonoidLaw:
  def checkGroupLaw[A](x: A, y: A, z: A)(using
      Group[A]
  ): ValidatedNel[String, Unit] =
    (
      checkMonoidLaw(x, y, z),
      check(
        Group[A].combine(x, Group[A].inverse(x)) == Group[A].empty,
        "inverse law 1: x + (-x) = 0"
      ),
      check(
        Group[A].combine(Group[A].inverse(x), x) == Group[A].empty,
        "inverse law 2: (-x) + x = 0"
      )
    ).mapN { case (_, _, _) => () }
