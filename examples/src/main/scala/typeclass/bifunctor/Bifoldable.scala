package typeclass.bifunctor

import typeclass.common.*
import typeclass.monad.Foldable
import typeclass.monoid.Monoid
import typeclass.monoid.Monoid.*

trait Bifoldable[F[_]]:
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

object Bifoldable:
  given idFoldable: Foldable[Id] with
    extension [A](fa: Id[A])
      override def foldRight[B](init: B)(f: (A, B) => B): B =
        f(fa.value, init)

  given Foldable[Option] with
    extension[A] (as: Option[A])
      override def foldRight[B](acc: B)(f: (A, B) => B) = as match
        case None => acc
        case Some(a) => f(a, acc)
      override def foldLeft[B](acc: B)(f: (B, A) => B) = as match
        case None => acc
        case Some(a) => f(acc, a)
      override def foldMap[B](f: A => B)(using mb: Monoid[B]): B =
        as match
          case None => mb.empty
          case Some(a) => f(a)

  given Foldable[List] with
    extension[A] (as: List[A])
      override def foldRight[B](acc: B)(f: (A, B) => B) =
        as.foldRight(acc)(f)
      override def foldLeft[B](acc: B)(f: (B, A) => B) =
        as.foldLeft(acc)(f)
      override def toList: List[A] = as

  given tuple2Foldable: Foldable[[X] =>> (X, X)] with
    extension [A](fa: (A, A))
      override def foldRight[B](init: B)(f: (A, B) => B): B =
        val (a0, a1) = fa
        val b = f(a1, init)
        f(a0, b)

  given tuple3Foldable: Foldable[[X] =>> (X, X, X)] with
    extension [A](fa: (A, A, A))
      override def foldRight[B](init: B)(f: (A, B) => B): B =
        val (a0, a1, a2) = fa
        val b0 = f(a2, init)
        val b1 = f(a1, b0)
        f(a0, b1)

  given eitherFoldable[E]: Foldable[[x] =>> Either[E, x]] with
    extension [A](fa: Either[E, A])
      override def foldRight[B](init: B)(f: (A, B) => B): B =
        fa match
          case Right(a) => f(a, init)
          case Left(_)  => init

  def foldRight[F[_], A, B](fa: F[A])(init: B)(f: (A, B) => B)(using foldable: Foldable[F]): B =
    foldable.foldRight(fa)(init)(f)
