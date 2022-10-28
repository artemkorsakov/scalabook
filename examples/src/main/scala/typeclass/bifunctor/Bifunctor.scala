package typeclass.bifunctor

import typeclass.common.*
import typeclass.monad.{Functor, InvariantFunctor}

trait Bifunctor[F[_, _]]:
  extension [A, B](fab: F[A, B])
    /** `map` over both type parameters. */
    def bimap[C, D](f: A => C, g: B => D): F[C, D]

    def leftMap[C](f: A => C): F[C, B] = fab.bimap(f, identity)

    def rightMap[D](g: B => D): F[A, D] = fab.bimap(identity, g)

  /** Extract the Functor on the first param. */
  def leftFunctor[R]: Functor[[X] =>> F[X, R]] =
    new Functor[[X] =>> F[X, R]]:
      extension [A](fax: F[A, R]) override def map[B](f: A => B): F[B, R] = leftMap(fax)(f)

  /** Extract the Functor on the second param. */
  def rightFunctor[L]: Functor[[X] =>> F[L, X]] =
    new Functor[[X] =>> F[L, X]]:
      extension [A](fax: F[L, A]) override def map[B](f: A => B): F[L, B] = rightMap(fax)(f)

  /** Unify the functor over both params. */
  def uFunctor: Functor[[X] =>> F[X, X]] =
    new Functor[[X] =>> F[X, X]]:
      extension [A](fax: F[A, A]) override def map[B](f: A => B): F[B, B] = umap(fax)(f)

  def umap[A, B](faa: F[A, A])(f: A => B): F[B, B] =
    bimap(faa)(f, f)

object Bifunctor:
  given Bifunctor[Either] with
    extension [A, B](fab: Either[A, B])
      override def bimap[C, D](f: A => C, g: B => D): Either[C, D] =
        fab match
          case Right(value) => Right(g(value))
          case Left(value)  => Left(f(value))

  given Bifunctor[Writer] with
    extension [A, B](fab: Writer[A, B])
      override def bimap[C, D](f: A => C, g: B => D): Writer[C, D] =
        Writer { () =>
          val (a, b) = fab.run()
          (f(a), g(b))
        }

  def bimap[F[_, _], A, B, C, D](fab: F[A, B])(f: A => C, g: B => D)(using bifunctor: Bifunctor[F]): F[C, D] =
    bifunctor.bimap(fab)(f, g)
