package typeclass.monad

import typeclass.monoid.Monoid

trait Divisible[F[_]] extends Divide[F], InvariantApplicative[F]:
  /** Universally quantified instance of F[_] */
  def conquer[A]: F[A]

  override def xunit0[A](a: => A): F[A] = conquer

  override def cmap[A, B](fb: F[B])(f: A => B): F[A] =
    divide(conquer[Unit], fb)(c => ((), f(c)))

object Divisible:
  def apply[F[_]: Divisible]: Divisible[F] = summon[Divisible[F]]

  given functionDivisible[R: Monoid]: Divisible[[X] =>> Function1[X, R]] with
    override def divide[A, B, C](fa: => A => R, fb: => B => R)(f: C => (A, B)): C => R =
      c => {
        val (a, b) = f(c)
        Monoid[R].combine(fa(a), fb(b))
      }

    override def conquer[A]: A => R = _ => Monoid[R].empty
