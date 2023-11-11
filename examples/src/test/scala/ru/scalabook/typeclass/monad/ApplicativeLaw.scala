package ru.scalabook.typeclass.monad

trait ApplicativeLaw extends ApplyLaw, munit.Assertions:
  def checkApplicativeLaw[F[_]: Applicative, A, B, C](x: A)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    val fa = Applicative[F].unit(x)
    checkApplyLaw[F, A, B, C](
      fa,
      Applicative[F].unit(f),
      Applicative[F].unit(g)
    ): Unit
    assertEquals(
      Applicative[F].apply[A, A](Applicative[F].unit(identity))(fa),
      fa,
      "identity"
    )
    assertEquals(
      Applicative[F].apply(Applicative[F].unit(f))(Applicative[F].unit(x)),
      Applicative[F].unit(f(x)),
      "homomorphism"
    )
    assertEquals(
      Applicative[F].apply[A, B](Applicative[F].unit(f))(
        Applicative[F].unit(x)
      ),
      Applicative[F].apply(Applicative[F].unit((f: A => B) => f(x)))(
        Applicative[F].unit(f)
      ),
      "interchange"
    )
    assertEquals(
      Applicative[F].map(Applicative[F].unit(x))(f),
      Applicative[F].unit(f(x))
    )
    assertEquals(
      Applicative[F].map(Applicative[F].unit(x))(f),
      Applicative[F].apply(Applicative[F].unit(f))(fa)
    )
