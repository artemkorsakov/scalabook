package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.State

trait Traverse[F[_]] extends Functor[F], Foldable[F]:
  self =>

  extension [A](fa: F[A])
    def traverse[G[_]: Applicative, B](f: A => G[B]): G[F[B]]

    override def map[B](f: A => B): F[B] = traverse(a => Id(f(a)))

    override def foldRight[B](init: B)(f: (A, B) => B): B =
      traverse(a => State[B, A]((b: B) => (f(a, b), a))).run(init)._1

  def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]] =
    fga.traverse(ga => ga)

object Traverse:
  def apply[F[_]: Traverse]: Traverse[F] = summon[Traverse[F]]

  given Traverse[Option] with
    extension[A] (fa: Option[A])
      override def traverse[G[_] : Applicative, B](f: A => G[B]): G[Option[B]] =
        fa match
          case Some(a) => f(a).map(Some(_))
          case None => Applicative[G].unit(None)

  given Traverse[List] with
    extension[A] (fa: List[A])
      override def traverse[G[_] : Applicative, B](f: A => G[B]): G[List[B]] =
        fa.foldRight(Applicative[G].unit(List[B]()))((a, acc) =>
          f(a).map2(acc)(_ :: _)
        )
