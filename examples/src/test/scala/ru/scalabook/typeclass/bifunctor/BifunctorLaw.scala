package ru.scalabook.typeclass.bifunctor

import cats.data.ValidatedNel
import ru.scalabook.typeclass.monad.FunctorLaw

trait BifunctorLaw extends FunctorLaw:
  def checkBifunctorLaw[F[_, _], L, R, A, B, C](
      far: F[A, R],
      fla: F[L, A],
      faa: F[A, A]
  )(using f: A => B, fReverse: B => A, g: B => C, gReverse: C => B)(using
      bi: Bifunctor[F]
  ): ValidatedNel[String, Unit] =
    checkFunctorLaw[[X] =>> F[X, R], A, B, C](far)(using
      bi.leftFunctor[R],
      f,
      fReverse,
      g,
      gReverse
    ) combine
      checkFunctorLaw[[X] =>> F[L, X], A, B, C](fla)(using
        bi.rightFunctor[L],
        f,
        fReverse,
        g,
        gReverse
      ) combine
      checkFunctorLaw[[X] =>> F[X, X], A, B, C](faa)(using
        bi.uFunctor,
        f,
        fReverse,
        g,
        gReverse
      )
