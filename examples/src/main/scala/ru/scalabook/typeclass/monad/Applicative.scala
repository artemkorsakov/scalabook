package ru.scalabook.typeclass.monad

import cats.Id
import ru.scalabook.typeclass.common.State

trait Applicative[F[_]] extends Apply[F], InvariantApplicative[F]:
  def unit[A](a: => A): F[A]
  override final def xunit0[A](a: => A): F[A] = unit(a)

  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  extension [A](fa: F[A])
    def map[B](f: A => B): F[B] =
      apply(unit(f))(fa)

    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      apply(apply(unit(f.curried))(fa))(fb)

object Applicative:
  def apply[F[_]: Applicative]: Applicative[F] = summon[Applicative[F]]

  given idApplicative: Applicative[Id] with
    def unit[A](a: => A): Id[A]                        = a
    def apply[A, B](fab: Id[A => B])(fa: Id[A]): Id[B] = fab(fa)

  given optionApplicative: Applicative[Option] with
    override def unit[A](a: => A): Option[A] = Some(a)

    override def apply[A, B](fab: Option[A => B])(fa: Option[A]): Option[B] =
      (fab, fa) match
        case (Some(aToB), Some(a)) => Some(aToB(a))
        case _                     => None

  given listApplicative: Applicative[List] with
    override def unit[A](a: => A): List[A] = List(a)

    override def apply[A, B](fab: List[A => B])(fa: List[A]): List[B] =
      fab match
        case f :: tail =>
          fa.map(f) ++ apply(tail)(fa)
        case Nil => Nil

  given stateApplicative[S]: Applicative[[X] =>> State[S, X]] with
    override def unit[A](a: => A): State[S, A] =
      State[S, A](s => (s, a))

    override def apply[A, B](fab: State[S, A => B])(
        fa: State[S, A]
    ): State[S, B] =
      State { s =>
        val (s0, aToB) = fab.run(s)
        val (s1, a)    = fa.run(s0)
        (s1, aToB(a))
      }

  given compositeApplicative[F[_]: Applicative, G[_]: Applicative]
      : Applicative[[X] =>> F[G[X]]] with
    override def unit[A](a: => A): F[G[A]] =
      Applicative[F].unit(Applicative[G].unit(a))

    override def apply[A, B](fab: F[G[A => B]])(fa: F[G[A]]): F[G[B]] =
      val tmp: F[G[A] => G[B]] =
        Applicative[F].map(fab)(ga2b => Applicative[G].apply(ga2b))
      Applicative[F].apply(tmp)(fa)

  given tupleApplicative[F[_]: Applicative, G[_]: Applicative]
      : Applicative[[X] =>> (F[X], G[X])] with
    type FG[A] = (F[A], G[A])

    override def unit[A](a: => A): FG[A] =
      (Applicative[F].unit(a), Applicative[G].unit(a))

    override def apply[A, B](fab: FG[A => B])(fa: FG[A]): FG[B] =
      (Applicative[F].apply(fab._1)(fa._1), Applicative[G].apply(fab._2)(fa._2))
end Applicative
