package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.monoid.*

object Laws:
  // Общие законы
  def checkAssociativity[A](x: A, y: A, z: A)(using
      s: Semigroup[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      s.combine(s.combine(x, y), z) == s.combine(x, s.combine(y, z)),
      (),
      "Не соблюдается ассоциативность: (x + y) + z должно быть равно x + (y + z)"
    ).toValidatedNel

  def checkRightIdentity[A](x: A)(using
      m: Monoid[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      m.combine(x, m.empty) == x,
      (),
      "Не соблюдается тождественность справа: x + e = x"
    ).toValidatedNel

  def checkLeftIdentity[A](x: A)(using
      m: Monoid[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      m.combine(m.empty, x) == x,
      (),
      "Не соблюдается тождественность слева: e + x = x"
    ).toValidatedNel

  def checkInverse[A](x: A)(using
      g: Group[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      g.combine(x.inverse, x) == g.empty &&
        g.combine(x, x.inverse) == g.empty,
      (),
      "Не соблюдается обратимость: x + (-x) = (-x) + x = e"
    ).toValidatedNel

  def checkIdempotency[A](x: A)(using s: Band[A]): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      s.combine(x, x) == x,
      (),
      "Не соблюдается идемпотентность: x + x должно быть равно x"
    ).toValidatedNel

  // Законы типовых классов
  def checkSemigroupLaw[A: Semigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkAssociativity[A](x, y, z)

  def checkMonoidLaw[A: Monoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkAssociativity[A](x, y, z)
      .combine(checkRightIdentity(x))
      .combine(checkLeftIdentity(x))

  def checkGroupLaw[A: Group](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkAssociativity[A](x, y, z)
      .combine(checkRightIdentity(x))
      .combine(checkLeftIdentity(x))
      .combine(checkInverse(x))

  def checkBandLaw[A: Band](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkAssociativity[A](x, y, z)
      .combine(checkIdempotency(x))

  def checkIdempotentMonoid[A: IdempotentMonoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkAssociativity[A](x, y, z)
      .combine(checkRightIdentity(x))
      .combine(checkLeftIdentity(x))
      .combine(checkIdempotency(x))
end Laws
