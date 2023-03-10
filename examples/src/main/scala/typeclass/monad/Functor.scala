package typeclass.monad

import algorithms.trees.BinaryTree
import algorithms.trees.BinaryTree.*
import typeclass.common.*

trait Functor[F[_]] extends InvariantFunctor[F]:
  extension [A](fa: F[A])
    def map[B](f: A => B): F[B]

    override def xmap[B](f: A => B, g: B => A): F[B] = fa.map(f)

  def lift[A, B](f: A => B): F[A] => F[B] = _.map(f)

  def mapply[A, B](a: A)(f: F[A => B]): F[B] = map(f)((ff: A => B) => ff(a))

  def fproduct[A, B](fa: F[A])(f: A => B): F[(A, B)] = map(fa)(a => (a, f(a)))

object Functor:
  def apply[F[_]: Functor]: Functor[F] = summon[Functor[F]]

  given idFunctor: Functor[Id] with
    extension [A](as: Id[A])
      override def map[B](f: A => B): Id[B] = Id(f(as.value))

  given optionFunctor: Functor[Option] with
    extension [A](optA: Option[A])
      override def map[B](f: A => B): Option[B] =
        optA match
          case Some(a) => Some(f(a))
          case None    => None

  given listFunctor: Functor[List] with
    extension [A](as: List[A])
      override def map[B](f: A => B): List[B] = as.map(f)

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

  given nestedFunctor[F[_], G[_]](using
      functorF: Functor[F],
      functorG: Functor[G]
  ): Functor[[X] =>> Nested[F, G, X]] with
    extension [A](fga: Nested[F, G, A])
      override def map[B](f: A => B): Nested[F, G, B] =
        Nested[F, G, B] {
          functorF.map(fga.value)(ga => functorG.map(ga)(f))
        }

  given Functor[IO] with
    extension [A](as: IO[A])
      override def map[B](f: A => B): IO[B] = IO { () => f(as.run()) }

  given Functor[BinaryTree] with
    extension [A](as: BinaryTree[A])
      override def map[B](f: A => B): BinaryTree[B] = as match
        case Leaf                   => Leaf
        case Branch(a, left, right) => Branch(f(a), left.map(f), right.map(f))
