package ru.scalabook.typeclass.monad

import cats.data.*
import ru.scalabook.typeclass.common.Laws

trait InvariantFunctorLaw extends Laws:
  def checkInvariantFunctorLaw[F[_]: InvariantFunctor, A, B, C](
      fa: F[A]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    check(
      InvariantFunctor[F].xmap(fa)(identity, identity) == fa,
      "identity: xmap(ma)(identity, identity) == ma"
    ) combine
      check(
        InvariantFunctor[F]
          .xmap(InvariantFunctor[F].xmap(fa)(f, fReverse))(g, gReverse) ==
          InvariantFunctor[F].xmap(fa)(g compose f, fReverse compose gReverse),
        "composition: xmap(xmap(ma, f1, g1), f2, g2) == xmap(ma, f2 compose f1, g1 compose g2)"
      )
