package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import ru.scalabook.typeclass.monad.InvariantFunctor
import cats.syntax.either.*

object MonadLaws:
  def checkInvariantFunctorLaw[F[_]: InvariantFunctor, A, B, C](fa: F[A])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkXMapIdentityLaw[F, A, B, C](fa) combine
      checkXMapСompositionLaw[F, A, B, C](fa)

  private def checkXMapIdentityLaw[F[_], A, B, C](fa: F[A])(using
      inf: InvariantFunctor[F]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      inf.xmap(fa)(identity, identity) == fa,
      (),
      "Не соблюдается тождественность: xmap(ma)(identity, identity) == ma"
    ).toValidatedNel

  private def checkXMapСompositionLaw[F[_], A, B, C](fa: F[A])(using
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
