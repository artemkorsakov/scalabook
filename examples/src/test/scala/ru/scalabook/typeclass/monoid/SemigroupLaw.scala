package ru.scalabook.typeclass.monoid

import cats.data.*
import cats.syntax.either.*
import ru.scalabook.typeclass.common.Laws

trait SemigroupLaw extends Laws:
  def checkSemigroupLaw[A: Semigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    check(
      Semigroup[A].combine(Semigroup[A].combine(x, y), z) ==
        Semigroup[A].combine(x, Semigroup[A].combine(y, z)),
      "associative: (x + y) + z == x + (y + z)"
    )
