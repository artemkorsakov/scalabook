package typeclass.monad

import typeclass.monoid.Monoid

trait Divide[F[_]] extends ContravariantFunctor[F]:
  def divide[A, B, C](fa: => F[A], fb: => F[B])(f: C => (A, B)): F[C]

  def tuple2[A, B](fa: => F[A], fb: => F[B]): F[(A, B)] =
    divide(fa, fb)(identity)

object Divide:
  def apply[F[_]: Divide]: Divide[F] = summon[Divide[F]]

  given functionDivide[R: Monoid]: Divide[[X] =>> Function1[X, R]] with
    override def cmap[A, B](function: B => R)(f: A => B): A => R =
      a => function(f(a))

    override def divide[A, B, C](fa: => A => R, fb: => B => R)(
        f: C => (A, B)
    ): C => R =
      c => {
        val (a, b) = f(c)
        Monoid[R].combine(fa(a), fb(b))
      }
