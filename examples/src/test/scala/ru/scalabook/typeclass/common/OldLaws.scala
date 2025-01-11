package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.monoid.*

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

  def checkMonoidLaw[A: Monoid](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z) combine
      check(
        Monoid[A].combine(x, Monoid[A].empty) == x,
        "right identity: x + e = x"
      ) combine
      check(
        Monoid[A].combine(Monoid[A].empty, x) == x,
        "left identity: e + x = x"
      )

  def checkGroupLaw[A: Group](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkMonoidLaw(x, y, z) combine
      check(
        Group[A].combine(x, Group[A].inverse(x)) == Group[A].empty,
        "inverse law 1: x + (-x) = 0"
      ) combine
      check(
        Group[A].combine(Group[A].inverse(x), x) == Group[A].empty,
        "inverse law 2: (-x) + x = 0"
      )

  def checkBandLaw[A: Band](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z) combine
      check(Band[A].combine(x, x) == x, "idempotency: x + x = x")

  def checkIdempotentMonoidLaw[A: IdempotentMonoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkMonoidLaw(x, y, z) combine checkBandLaw(x, y, z)

  def checkCSemigroupLaw[A: CSemigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z) combine
      check(
        CSemigroup[A].combine(x, y) == CSemigroup[A].combine(y, x),
        "commutative: x + y = y + x"
      )

  def checkCMonoidLaw[A: CMonoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkMonoidLaw(x, y, z) combine checkCSemigroupLaw(x, y, z)
end OldLaws
