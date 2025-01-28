package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.monoid.Monoid
import ru.scalabook.typeclass.monoid.Monoid.{dual, endoMonoid}

trait Foldable[F[_]]:
  extension [A](fa: F[A])
    def foldRight[B](init: B)(f: (A, B) => B): B =
      fa.foldMap(f.curried)(using endoMonoid[B])(init)

    def foldLeft[B](acc: B)(f: (B, A) => B): B =
      fa.foldMap(a => b => f(b, a))(using dual(endoMonoid[B]))(acc)

    def foldMap[B](f: A => B)(using mb: Monoid[B]): B =
      fa.foldRight(mb.empty)((a, b) => mb.combine(f(a), b))

    def combineAll(using ma: Monoid[A]): A =
      fa.foldLeft(ma.empty)(ma.combine)

    def toList: List[A] =
      fa.foldRight(List.empty[A])(_ :: _)

object Foldable:
  def apply[F[_]: Foldable]: Foldable[F] = summon[Foldable[F]]

  given Foldable[Id] with
    extension [A](fa: Id[A])
      override def foldRight[B](init: B)(f: (A, B) => B): B =
        f(fa, init)

  given Foldable[Option] with
    extension [A](as: Option[A])
      override def foldRight[B](acc: B)(f: (A, B) => B): B =
        as match
          case None    => acc
          case Some(a) => f(a, acc)
      override def foldLeft[B](acc: B)(f: (B, A) => B): B =
        as match
          case None    => acc
          case Some(a) => f(acc, a)
      override def foldMap[B](f: A => B)(using mb: Monoid[B]): B =
        as match
          case None    => mb.empty
          case Some(a) => f(a)
end Foldable
