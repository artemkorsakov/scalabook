package typeclass.monad

import typeclass.common.Runner1
import typeclass.monad.Applicative.{apply, map, unit}

trait ApplicativeLaw extends ApplyLaw:
  def checkApplicativeLaw[F[_]: Applicative, A, B, C](x: A)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    val fa = unit(x)
    checkApplyLaw[F, A, B, C](fa, unit(f), unit(g))
    assertEquals(apply[F, A, A](unit(identity))(fa), fa, "identity")
    assertEquals(apply(unit(f))(unit(x)), unit(f(x)), "homomorphism")
    assertEquals(
      apply[F, A, B](unit(f))(unit(x)),
      apply(unit((f: A => B) => f(x)))(unit(f)),
      "interchange"
    )
    assertEquals(map(unit(x), f), unit(f(x)))
    assertEquals(map(unit(x), f), apply(unit(f))(fa))

  def checkApplicativeLawWithRunner[F[_]: Applicative: Runner1, A, B, C](x: A)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    val fa = unit(x)
    checkApplyLawWithRunner[F, A, B, C](fa, unit(f), unit(g))
    assertEquals(Runner1[F].run(apply[F, A, A](unit(identity))(fa)), Runner1[F].run(fa), "identity")
    assertEquals(Runner1[F].run(apply(unit(f))(unit(x))), Runner1[F].run(unit(f(x))), "homomorphism")
    assertEquals(
      Runner1[F].run(apply[F, A, B](unit(f))(unit(x))),
      Runner1[F].run(apply(unit((f: A => B) => f(x)))(unit(f))),
      "interchange"
    )
    assertEquals(Runner1[F].run(map(unit(x), f)), Runner1[F].run(unit(f(x))))
    assertEquals(Runner1[F].run(map(unit(x), f)), Runner1[F].run(apply(unit(f))(fa)))
