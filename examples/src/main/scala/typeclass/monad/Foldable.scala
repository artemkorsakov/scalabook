package typeclass.monad

import typeclass.common.*

trait Foldable[F[_]]:
  extension [A](fa: F[A]) def foldRight[B](init: B)(f: (A, B) => B): B

object Foldable:
  given idFoldable: Foldable[Id] with
    extension [A](fa: Id[A])
      override def foldRight[B](init: B)(f: (A, B) => B): B =
        f(fa.value, init)

  given optionFoldable: Foldable[Option] with
    extension [A](fa: Option[A])
      override def foldRight[B](init: B)(f: (A, B) => B): B =
        fa match
          case Some(a) => f(a, init)
          case None    => init

  given listFoldable: Foldable[List] with
    extension [A](as: List[A])
      override def foldRight[B](init: B)(f: (A, B) => B): B =
        as.foldRight(init)(f)

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
