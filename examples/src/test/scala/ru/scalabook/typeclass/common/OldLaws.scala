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

  def checkAbGroupLaw[A: AbGroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkGroupLaw(x, y, z) combine checkCMonoidLaw(x, y, z)

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

  def checkSemiringLaw[A: Semiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkCMonoidLaw(x, y, z) combine
      checkMultiplicativeSemigroupLaw(x, y, z) combine
      check(
        Semiring[A].times(x, Semiring[A].combine(y, z)) ==
          Semiring[A].combine(Semiring[A].times(x, y), Semiring[A].times(x, z)),
        "left distributivus: x * (y + z) = x * y + x * z"
      ) combine
      check(
        Semiring[A].times(Semiring[A].combine(x, y), z) ==
          Semiring[A].combine(Semiring[A].times(x, z), Semiring[A].times(y, z)),
        "right distributivus: (x + y) * z = x * z + y * z"
      ) combine
      check(
        Semiring[A].times(x, Semiring[A].empty) == Semiring[A].empty,
        "left zero multiplicative: a * 0 = 0"
      ) combine
      check(
        Semiring[A].times(Semiring[A].empty, x) == Semiring[A].empty,
        "right zero multiplicative: 0 * a = 0"
      )

  def checkSemiringWithUnityLaw[A: SemiringWithUnity](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemiringLaw(x, y, z) combine
      check(
        SemiringWithUnity[A].times(x, SemiringWithUnity[A].one) == x,
        "times right identity: x * 1 = x"
      ) combine
      check(
        SemiringWithUnity[A].times(SemiringWithUnity[A].one, x) == x,
        "times left identity: 1 * x = x"
      )

  def checkCSemiringLaw[A: CSemiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemiringLaw(x, y, z) combine
      check(
        CSemiring[A].times(x, y) == CSemiring[A].times(y, x),
        "commutative: x * y = y * x"
      )
end OldLaws
