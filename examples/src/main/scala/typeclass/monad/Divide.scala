package typeclass.monad

import typeclass.monoid.Monoid

trait Divide[F[_]] extends ContravariantFunctor[F]:
  def divide[A, B, C](fa: => F[A], fb: => F[B])(f: C => (A, B)): F[C]

  def tuple2[A, B](fa: => F[A], fb: => F[B]): F[(A, B)] = divide(fa, fb)(identity)

object Divide:
  given functionDivide[R: Monoid]: Divide[[X] =>> Function1[X, R]] with
    override def cmap[A, B](function: B => R)(f: A => B): A => R =
      a => function(f(a))

    override def divide[A, B, C](fa: => A => R, fb: => B => R)(f: C => (A, B)): C => R =
      c => {
        val (a, b) = f(c)
        summon[Monoid[R]].combine(fa(a), fb(b))
      }

  def divide[F[_], A, B, C](fa: => F[A], fb: => F[B])(f: C => (A, B))(using di: Divide[F]): F[C] =
    di.divide(fa, fb)(f)
