package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel

trait MonadLaw extends ApplicativeLaw, BindLaw:
  def checkMonadLaw[F[_]: Monad, A, B, C](x: A)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    val fa             = Monad[F].unit(x)
    val afb: A => F[B] = a => Monad[F].unit(f(a))
    val bfc: B => F[C] = b => Monad[F].unit(g(b))
    checkApplicativeLaw[F, A, B, C](x) combine
      checkBindLaw[F, A, B, C](
        fa,
        Monad[F].unit(f),
        Monad[F].unit(g),
        afb,
        bfc
      ) combine
      check(Monad[F].unit(x).flatMap(afb) == afb(x), "left identity") combine
      check(fa.flatMap(x => Monad[F].unit(x)) == fa, "right identity")
end MonadLaw
