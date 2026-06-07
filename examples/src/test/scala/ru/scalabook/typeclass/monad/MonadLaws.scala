package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.common.BasicLaws
import ru.scalabook.typeclass.monoid.Monoid

object MonadLaws extends BasicLaws:
  def checkFoldableLaw[F[_]: Foldable, A](fa: F[A])(using
      Monoid[Vector[A]]
  ): ValidatedNel[String, Unit] =
    checkFoldLeftMatchesFoldMap(fa) combine
      checkFoldRightMatchesFoldMap(fa)

  def checkFunctorLaw[F[_]: Functor, A, B, C](fa: F[A])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkInvariantFunctorLaw[F, A, B, C](fa) combine
      checkMapIdentityLaw[F, A](fa) combine
      checkMapCompositionLaw[F, A, B, C](fa)

  def checkInvariantFunctorLaw[F[_]: InvariantFunctor, A, B, C](fa: F[A])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkXMapIdentityLaw[F, A](fa) combine
      checkXMapCompositionLaw[F, A, B, C](fa)

  private def checkFoldLeftMatchesFoldMap[F[_]: Foldable, A](fa: F[A])(using
      Monoid[Vector[A]]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      fa.foldMap(Vector(_)) == fa.foldLeft(Vector.empty[A])(_ :+ _),
      (),
      "`foldLeft` не соответствует `foldMap`, не соблюдается: `fa.foldMap(Vector(_)) == fa.foldLeft(Vector.empty[A])(_ :+ _)`"
    ).toValidatedNel

  private def checkFoldRightMatchesFoldMap[F[_]: Foldable, A](fa: F[A])(using
      Monoid[Vector[A]]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      fa.foldMap(Vector(_)) == fa.foldRight(Vector.empty[A])(_ +: _),
      (),
      "`foldRight` соответствует `foldMap`, не соблюдается: `fa.foldMap(Vector(_)) == fa.foldRight(Vector.empty[A])(_ +: _)`"
    ).toValidatedNel

  private def checkMapIdentityLaw[F[_], A](fa: F[A])(using
      fu: Functor[F]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      fu.map(fa)(identity) == fa,
      (),
      "Не соблюдается тождественность: map(fa)(identity) == fa"
    ).toValidatedNel

  private def checkMapCompositionLaw[F[_], A, B, C](fa: F[A])(using
      fu: Functor[F],
      f: A => B,
      g: B => C
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      fu.map(fu.map(fa)(f))(g) == fu.map(fa)(f.andThen(g)),
      (),
      "Не соблюдается композиция: fa.map(f).map(g) == fa.map(g(f(_)))"
    ).toValidatedNel

  private def checkXMapIdentityLaw[F[_], A](fa: F[A])(using
      inf: InvariantFunctor[F]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      inf.xmap(fa)(identity, identity) == fa,
      (),
      "Не соблюдается тождественность: xmap(ma)(identity, identity) == ma"
    ).toValidatedNel

  private def checkXMapCompositionLaw[F[_], A, B, C](fa: F[A])(using
      inf: InvariantFunctor[F],
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      inf.xmap(inf.xmap(fa)(f, fReverse))(g, gReverse) ==
        inf.xmap(fa)(g compose f, fReverse compose gReverse),
      (),
      "Не соблюдается композиция: xmap(xmap(ma, f1, g1), f2, g2) == xmap(ma, f2 compose f1, g1 compose g2)"
    ).toValidatedNel
end MonadLaws
