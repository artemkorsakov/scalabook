package typeclass.monad

import typeclass.common.State
import typeclass.monad.Applicative.{apply, map, unit}

trait ApplicativeLaw extends ApplyLaw:
  def checkApplicativeLaw[F[_], A, B, C](x: A)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using Applicative[F]): Unit =
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

  def checkApplicativeLaw[F[_], A, B, C](x: A, run: F[A] | F[B] | F[C] => A | B | C)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using Applicative[F]): Unit =
    val fa = unit(x)
    checkApplyLaw[F, A, B, C](fa, unit(f), unit(g), run)
    assertEquals(run(apply[F, A, A](unit(identity))(fa)), run(fa), "identity")
    assertEquals(run(apply(unit(f))(unit(x))), run(unit(f(x))), "homomorphism")
    assertEquals(
      run(apply[F, A, B](unit(f))(unit(x))),
      run(apply(unit((f: A => B) => f(x)))(unit(f))),
      "interchange"
    )
    assertEquals(run(map(unit(x), f)), run(unit(f(x))))
    assertEquals(run(map(unit(x), f)), run(apply(unit(f))(fa)))
