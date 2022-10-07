package typeclass.monad

trait InvariantApplicative[F[_]] extends InvariantFunctor[F]:
  def xunit0[A](a: => A): F[A]

  extension [A](fa: => F[A])
    def xunit1[B](f: A => B, g: B => A): F[B] =
      fa.xmap(f, g)
