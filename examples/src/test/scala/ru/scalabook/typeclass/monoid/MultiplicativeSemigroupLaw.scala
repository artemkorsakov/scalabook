package ru.scalabook.typeclass.monoid

import cats.data.*
import ru.scalabook.typeclass.common.Laws

trait MultiplicativeSemigroupLaw extends Laws:
  def checkMultiplicativeSemigroupLaw[A: MultiplicativeSemigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    check(
      MultiplicativeSemigroup[A]
        .times(MultiplicativeSemigroup[A].times(x, y), z) ==
        MultiplicativeSemigroup[A]
          .times(x, MultiplicativeSemigroup[A].times(y, z)),
      "associative: (x * y) * z == x * (y * z)"
    )
