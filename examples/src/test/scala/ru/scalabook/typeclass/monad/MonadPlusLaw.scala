package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel

trait MonadPlusLaw extends ApplicativePlusLaw:
  def checkMonadPlusLaw[F[_]: MonadPlus, A, B, C](
      x: A,
      f1: F[A],
      f2: F[A],
      f3: F[A]
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): ValidatedNel[String, Unit] =
    checkMonadLaw[F, A, B, C](x) combine
      checkApplicativePlusLaw[F, A, B, C](x, f1, f2, f3) combine
      check(
        MonadPlus[F].empty[A].map(_ => x) ==
          MonadPlus[F].empty[A],
        "`empty[A]` is a polymorphic value over `A`"
      ) combine
      check(
        MonadPlus[F].empty[A].flatMap(_ => f1) ==
          MonadPlus[F].empty[A],
        "`empty` short-circuits its right"
      ) combine
      check(
        f1.flatMap(_ => MonadPlus[F].empty[A]) ==
          MonadPlus[F].empty[A],
        "`empty` short-circuits throughout its `join` tree"
      )
end MonadPlusLaw
