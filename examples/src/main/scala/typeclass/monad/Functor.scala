package typeclass.monad

import typeclass.common.{Id, State, Writer}

trait Functor[F[_]]:
  extension [A](fa: F[A]) def map[B](f: A => B): F[B]

object Functor:
  given idFunctor: Functor[Id] with
    extension [A](as: Id[A]) override def map[B](f: A => B): Id[B] = Id(f(as.value))

  given optionFunctor: Functor[Option] with
    extension [A](optA: Option[A])
      override def map[B](f: A => B): Option[B] =
        optA match
          case Some(a) => Some(f(a))
          case None    => None

  given listFunctor: Functor[List] with
    extension [A](as: List[A]) override def map[B](f: A => B): List[B] = as.map(f)

  given eitherFunctor[E]: Functor[[x] =>> Either[E, x]] with
    extension [A](fa: Either[E, A])
      override def map[B](f: A => B): Either[E, B] =
        fa match
          case Right(a) => Right(f(a))
          case Left(e)  => Left(e)

  given writerFunctor[W]: Functor[[x] =>> Writer[W, x]] with
    extension [A](fa: Writer[W, A])
      override def map[B](f: A => B): Writer[W, B] =
        val (w, a) = fa.run()
        Writer[W, B](() => (w, f(a)))

  given stateFunctor[S]: Functor[[x] =>> State[S, x]] with
    extension [A](fa: State[S, A])
      override def map[B](f: A => B): State[S, B] =
        State[S, B] { s =>
          val (s1, a) = fa.run(s)
          (s1, f(a))
        }

  def map[F[_], A, B](fa: F[A], f: A => B)(using functor: Functor[F]): F[B] =
    functor.map(fa)(f)
