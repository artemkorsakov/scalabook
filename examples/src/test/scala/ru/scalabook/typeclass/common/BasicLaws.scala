package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.monad.{Plus, PlusEmpty}
import ru.scalabook.typeclass.monoid.*

trait BasicLaws:
  def check(condition: Boolean, message: String): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](condition, (), message).toValidatedNel

  def checkAssociativity[A](x: A, y: A, z: A)(using
      s: Semigroup[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      s.combine(s.combine(x, y), z) == s.combine(x, s.combine(y, z)),
      (),
      "Не соблюдается ассоциативность: (x + y) + z должно быть равно x + (y + z)"
    ).toValidatedNel

  def checkPlusAssociativity[F[_], A](f1: F[A], f2: F[A], f3: F[A])(using
      pl: Plus[F]
  ): ValidatedNel[String, Unit] =
    import pl.*
    Either.cond[String, Unit](
      plus(plus(f1, f2), f3) == plus(f1, plus(f2, f3)),
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

  def checkIdentity[A: Monoid](x: A): ValidatedNel[String, Unit] =
    checkRightIdentity(x).combine(checkLeftIdentity(x))

  def checkPlusEmptyRightIdentity[F[_], A](fa: F[A])(using
      pl: PlusEmpty[F]
  ): ValidatedNel[String, Unit] =
    import pl.*
    Either.cond[String, Unit](
      plus(fa, empty) == fa,
      (),
      "Не соблюдается тождественность справа: x + e = x"
    ).toValidatedNel

  def checkPlusEmptyLeftIdentity[F[_], A](fa: F[A])(using
      pl: PlusEmpty[F]
  ): ValidatedNel[String, Unit] =
    import pl.*
    Either.cond[String, Unit](
      plus(empty, fa) == fa,
      (),
      "Не соблюдается тождественность слева: e + x = x"
    ).toValidatedNel

  def checkPlusEmptyIdentity[F[_]: PlusEmpty, A](fa: F[A])
      : ValidatedNel[String, Unit] =
    checkPlusEmptyRightIdentity(fa).combine(checkPlusEmptyLeftIdentity(fa))

end BasicLaws
