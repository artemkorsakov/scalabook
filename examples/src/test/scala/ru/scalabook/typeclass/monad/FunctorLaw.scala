package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel
import ru.scalabook.typeclass.common.OldLaws

trait FunctorLaw extends OldLaws:
  def checkFunctorLaw[F[_]: Functor, A, B, C](
      fa: F[A]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkInvariantFunctorLaw[F, A, B, C](fa) combine
      check(
        Functor[F].map(fa)(identity) == fa,
        "identity: map(fa)(identity) == fa"
      ) combine
      check(
        Functor[F].map(Functor[F].map(fa)(f))(g) ==
          Functor[F].map(fa)(f.andThen(g)),
        "composition: fa.map(f).map(g) == fa.map(g(f(_)))"
      )
end FunctorLaw
