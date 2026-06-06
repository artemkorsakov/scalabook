package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.State
import ru.scalabook.typeclass.monad.Traverse.stateApplicative
import ru.scalabook.typeclass.monoid.Monoid

trait Traverse[F[_]] extends Functor[F], Foldable[F]:
  self =>

  extension [A](fa: F[A])
    def traverse[G[_]: Applicative, B](f: A => G[B]): G[F[B]]

    override def map[B](f: A => B): F[B] = traverse(a => Id(f(a)))

    override def foldMap[B](f: A => B)(using mb: Monoid[B]): B =
      val state: State[B, F[A]] =
        traverse(a => State[B, A]((b: B) => (mb.combine(b, f(a)), a)))(using
          stateApplicative
        )
      val (b, _) = state.run(mb.empty)
      b

  def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]] =
    fga.traverse(ga => ga)
end Traverse

object Traverse:
  def apply[F[_]: Traverse]: Traverse[F] = summon[Traverse[F]]

  given stateApplicative[S]: Applicative[[X] =>> State[S, X]] with
    def unit[A](a: => A): State[S, A] =
      State[S, A](s => (s, a))

    def apply[A, B](fab: State[S, A => B])(
        fa: State[S, A]
    ): State[S, B] =
      State { s =>
        val (s0, aToB) = fab.run(s)
        val (s1, a)    = fa.run(s0)
        (s1, aToB(a))
      }

  given Traverse[Option] with
    extension [A](fa: Option[A])
      override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Option[B]] =
        fa match
          case Some(a) => f(a).map(Some(_))
          case None    => Applicative[G].unit(None)

  given Traverse[List] with
    extension [A](fa: List[A])
      override def traverse[G[_]: Applicative, B](f: A => G[B]): G[List[B]] =
        fa.foldRight(Applicative[G].unit(List[B]()))((a, acc) =>
          f(a).map2(acc)(_ :: _)
        )
end Traverse
