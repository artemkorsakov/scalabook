package typeclass.monad

import munit.Assertions
import typeclass.monoid.Monoid

trait FoldableLaw extends Assertions:
  def checkFoldableLaw[F[_], A](fa: F[A])(using Foldable[F], Monoid[Vector[A]]): Unit =
    assertEquals(fa.foldMap(Vector(_)), fa.foldLeft(Vector.empty[A])(_ :+ _))
    assertEquals(fa.foldMap(Vector(_)), fa.foldRight(Vector.empty[A])(_ +: _))
