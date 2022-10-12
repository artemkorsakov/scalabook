package typeclass.monad

import typeclass.monoid.Semigroup

trait Plus[F[_]]:
  def plus[A](fa1: F[A], fa2: => F[A]): F[A]

  def semigroup[A]: Semigroup[F[A]] = (f1: F[A], f2: F[A]) => plus(f1, f2)

object Plus:
  given Plus[List] = new Plus[List]:
    def plus[A](fa1: List[A], fa2: => List[A]): List[A] = fa1 ++ fa2
