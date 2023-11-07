package ru.scalabook.typeclass.monoid

import cats.data.*
import cats.syntax.apply.*

trait MonoidLaw extends SemigroupLaw:
  def checkMonoidLaw[A](x: A, y: A, z: A)(using
      Monoid[A]
  ): ValidatedNel[String, Unit] =
    (
      checkSemigroupLaw(x, y, z),
      check(
        Monoid[A].combine(x, Monoid[A].empty) == x,
        "right identity: x + e = x"
      ),
      check(
        Monoid[A].combine(Monoid[A].empty, x) == x,
        "left identity: e + x = x"
      )
    ).mapN { case (_, _, _) => () }
