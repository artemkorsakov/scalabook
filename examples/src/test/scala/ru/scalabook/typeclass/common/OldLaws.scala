package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.monoid.Semigroup

trait OldLaws:
  protected def check(law: Boolean): ValidatedNel[String, Unit] =
    check(law, "")

  protected def check(
      law: Boolean,
      message: => String
  ): ValidatedNel[String, Unit] =
    Either
      .cond[String, Unit](law, (), message)
      .toValidatedNel

  protected def checkSemigroupLaw[A: Semigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    val s = summon[Semigroup[A]]
    check(
      s.combine(s.combine(x, y), z) == s.combine(x, s.combine(y, z)),
      "associative: (x + y) + z == x + (y + z)"
    )
end OldLaws
