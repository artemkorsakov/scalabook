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

  def checkApplicativeLawForState[A, B, C](x: A, s: String)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using Applicative[[X] =>> State[String, X]]): Unit =
    val fa = unit(x)
    checkApplyLawForState[A, B, C](fa, unit(f), unit(g), s)
    // assertEquals(apply[F, A, A](unit(identity))(fa), fa, "identity")
    // assertEquals(apply(unit(f))(unit(x)), unit(f(x)), "homomorphism")
    // assertEquals(
    //  apply[F, A, B](unit(f))(unit(x)),
    //  apply(unit((f: A => B) => f(x)))(unit(f)),
    //  "interchange"
    // )
    // assertEquals(map(unit(x), f), unit(f(x)))
    // assertEquals(map(unit(x), f), apply(unit(f))(fa))
