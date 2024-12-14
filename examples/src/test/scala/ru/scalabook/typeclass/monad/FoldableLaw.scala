package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel
import ru.scalabook.typeclass.common.OldLaws
import ru.scalabook.typeclass.monoid.Monoid

trait FoldableLaw extends OldLaws:
  def checkFoldableLaw[F[_]: Foldable, A](fa: F[A])(using
      Monoid[Vector[A]]
  ): ValidatedNel[String, Unit] =
    check(fa.foldMap(Vector(_)) == fa.foldLeft(Vector.empty[A])(_ :+ _)) combine
      check(fa.foldMap(Vector(_)) == fa.foldRight(Vector.empty[A])(_ +: _))
