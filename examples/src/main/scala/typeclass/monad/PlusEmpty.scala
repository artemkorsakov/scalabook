package typeclass.monad

import typeclass.monoid.Monoid

trait PlusEmpty[F[_]] extends Plus[F]:
  self =>

  def empty[A]: F[A]

  def monoid[A]: Monoid[F[A]] =
    new Monoid[F[A]]:
      override def combine(f1: F[A], f2: F[A]): F[A] = plus(f1, f2)
      override def empty: F[A]                       = self.empty[A]

object PlusEmpty:
  def apply[F[_]: PlusEmpty]: PlusEmpty[F] = summon[PlusEmpty[F]]

  given PlusEmpty[List] with
    def plus[A](fa1: List[A], fa2: => List[A]): List[A] = fa1 ++ fa2
    def empty[A]: List[A]                               = List.empty[A]
