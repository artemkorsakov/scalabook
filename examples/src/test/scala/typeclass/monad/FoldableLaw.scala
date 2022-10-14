package typeclass.monad

import munit.Assertions
import typeclass.monoid.Monoid

trait FoldableLaw extends Assertions:
  def checkFoldableLaw[F[_]: Foldable, A](fa: F[A])(using Monoid[Vector[A]]): Unit =
    assertEquals(fa.foldMap(Vector(_)), fa.foldLeft(Vector.empty[A])(_ :+ _))
    assertEquals(fa.foldMap(Vector(_)), fa.foldRight(Vector.empty[A])(_ +: _))
