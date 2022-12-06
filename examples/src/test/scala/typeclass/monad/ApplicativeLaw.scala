package typeclass.monad

import typeclass.common.Runner1

trait ApplicativeLaw extends ApplyLaw:
  def checkApplicativeLaw[F[_]: Applicative, A, B, C](x: A)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    val fa = Applicative[F].unit(x)
    checkApplyLaw[F, A, B, C](fa, Applicative[F].unit(f), Applicative[F].unit(g))
    assertEquals(Applicative[F].apply[A, A](Applicative[F].unit(identity))(fa), fa, "identity")
    assertEquals(
      Applicative[F].apply(Applicative[F].unit(f))(Applicative[F].unit(x)),
      Applicative[F].unit(f(x)),
      "homomorphism"
    )
    assertEquals(
      Applicative[F].apply[A, B](Applicative[F].unit(f))(Applicative[F].unit(x)),
      Applicative[F].apply(Applicative[F].unit((f: A => B) => f(x)))(Applicative[F].unit(f)),
      "interchange"
    )
    assertEquals(Applicative[F].map(Applicative[F].unit(x))(f), Applicative[F].unit(f(x)))
    assertEquals(Applicative[F].map(Applicative[F].unit(x))(f), Applicative[F].apply(Applicative[F].unit(f))(fa))

  def checkApplicativeLawWithRunner[F[_]: Applicative: Runner1, A, B, C](x: A)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    val fa = Applicative[F].unit(x)
    checkApplyLawWithRunner[F, A, B, C](fa, Applicative[F].unit(f), Applicative[F].unit(g))
    assertEquals(
      Runner1[F].run(Applicative[F].apply[A, A](Applicative[F].unit(identity))(fa)),
      Runner1[F].run(fa),
      "identity"
    )
    assertEquals(
      Runner1[F].run(Applicative[F].apply(Applicative[F].unit(f))(Applicative[F].unit(x))),
      Runner1[F].run(Applicative[F].unit(f(x))),
      "homomorphism"
    )
    assertEquals(
      Runner1[F].run(Applicative[F].apply[A, B](Applicative[F].unit(f))(Applicative[F].unit(x))),
      Runner1[F].run(Applicative[F].apply(Applicative[F].unit((f: A => B) => f(x)))(Applicative[F].unit(f))),
      "interchange"
    )
    assertEquals(
      Runner1[F].run(Applicative[F].map(Applicative[F].unit(x))(f)),
      Runner1[F].run(Applicative[F].unit(f(x)))
    )
    assertEquals(
      Runner1[F].run(Applicative[F].map(Applicative[F].unit(x))(f)),
      Runner1[F].run(Applicative[F].apply(Applicative[F].unit(f))(fa))
    )
