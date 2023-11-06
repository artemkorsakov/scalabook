package ru.scalabook.typeclass.monoid

import cats.data.*
import cats.syntax.either.*

trait SemigroupLaw:
  def checkSemigroupLaw[A](x: A, y: A, z: A)(using
      sg: Semigroup[A]
  ): ValidatedNel[String, Unit] =
    import sg.combine
    Either
      .cond[String, Unit](
        combine(combine(x, y), z) == combine(x, combine(y, z)),
        (),
        "combine must be associative: (x + y) + z == x + (y + z)"
      )
      .toValidatedNel
