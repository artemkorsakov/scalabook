package ru.scalabook.typeclass.bifunctor

import cats.data.ValidatedNel
import ru.scalabook.typeclass.monad.FoldableLaw
import ru.scalabook.typeclass.monoid.Monoid

trait BifoldableLaw extends FoldableLaw:
  def checkBifoldableLaw[F[_, _], L, R, A](
      far: F[A, R],
      fla: F[L, A],
      faa: F[A, A]
  )(using bi: Bifoldable[F])(using
      mv: Monoid[Vector[A]]
  ): ValidatedNel[String, Unit] =
    checkFoldableLaw[[X] =>> F[X, R], A](far)(using
      bi.leftFoldable[R],
      mv
    ) combine
      checkFoldableLaw[[X] =>> F[L, X], A](fla)(using
        bi.rightFoldable[L],
        mv
      ) combine
      checkFoldableLaw[[X] =>> F[X, X], A](faa)(using bi.uFoldable, mv)
