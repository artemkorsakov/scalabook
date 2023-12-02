package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel

trait ApplicativeLaw extends ApplyLaw:
  def checkApplicativeLaw[F[_]: Applicative, A, B, C](x: A)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    val fa = Applicative[F].unit(x)
    checkApplyLaw[F, A, B, C](
      fa,
      Applicative[F].unit(f),
      Applicative[F].unit(g)
    ) combine
      check(
        Applicative[F].apply[A, A](Applicative[F].unit(identity))(fa) == fa,
        "identity"
      ) combine
      check(
        Applicative[F].apply(Applicative[F].unit(f))(Applicative[F].unit(x)) ==
          Applicative[F].unit(f(x)),
        "homomorphism"
      ) combine
      check(
        Applicative[F].apply[A, B](Applicative[F].unit(f))(
          Applicative[F].unit(x)
        ) ==
          Applicative[F].apply(Applicative[F].unit((f: A => B) => f(x)))(
            Applicative[F].unit(f)
          ),
        "interchange"
      ) combine
      check(
        Applicative[F].map(Applicative[F].unit(x))(f) ==
          Applicative[F].unit(f(x)),
        "identity"
      ) combine
      check(
        Applicative[F].map(Applicative[F].unit(x))(f) ==
          Applicative[F].apply(Applicative[F].unit(f))(fa),
        "identity"
      )
  end checkApplicativeLaw
end ApplicativeLaw
