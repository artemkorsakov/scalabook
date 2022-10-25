package typeclass.monad

import typeclass.monoid.Monoid

trait Divisible[F[_]] extends Divide[F], InvariantApplicative[F]:
  /** Universally quantified instance of F[_] */
  def conquer[A]: F[A]

  override def xunit0[A](a: => A): F[A] = conquer

  override def cmap[A, B](fb: F[B])(f: A => B): F[A] =
    divide(conquer[Unit], fb)(c => ((), f(c)))

object Divisible:
  given functionDivisible[R: Monoid]: Divisible[[X] =>> Function1[X, R]] with
    override def divide[A, B, C](fa: => A => R, fb: => B => R)(f: C => (A, B)): C => R =
      c => {
        val (a, b) = f(c)
        summon[Monoid[R]].combine(fa(a), fb(b))
      }

    override def conquer[A]: A => R = _ => summon[Monoid[R]].empty

  def divide[F[_], A, B, C](fa: => F[A], fb: => F[B])(f: C => (A, B))(using di: Divisible[F]): F[C] =
    di.divide(fa, fb)(f)

  def conquer[F[_], A](using di: Divisible[F]): F[A] = di.conquer
