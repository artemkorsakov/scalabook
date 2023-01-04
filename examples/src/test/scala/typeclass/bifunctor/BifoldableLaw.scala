package typeclass.bifunctor

import munit.Assertions
import typeclass.monad.{Foldable, FoldableLaw}
import typeclass.monoid.Monoid

trait BifoldableLaw extends FoldableLaw:
  def checkBifoldableLaw[F[_, _], L, R, A](
      far: F[A, R],
      fla: F[L, A],
      faa: F[A, A]
  )(using bi: Bifoldable[F])(using
      mv: Monoid[Vector[A]]
  ): Unit =
    checkFoldableLaw[[X] =>> F[X, R], A](far)(using bi.leftFoldable[R], mv)
    checkFoldableLaw[[X] =>> F[L, X], A](fla)(using bi.rightFoldable[L], mv)
    checkFoldableLaw[[X] =>> F[X, X], A](faa)(using bi.uFoldable, mv)
