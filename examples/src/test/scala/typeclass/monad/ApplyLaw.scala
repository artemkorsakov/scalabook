package typeclass.monad

import typeclass.common.State
import typeclass.monad.Apply.{apply, map}

trait ApplyLaw extends FunctorLaw:
  def checkApplyLaw[F[_], A, B, C](fa: F[A], fab: F[A => B], fbc: F[B => C])(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using Apply[F]): Unit =
    checkFunctorLaw[F, A, B, C](fa)
    assertEquals(
      apply(fbc)(apply(fab)(fa)),
      apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa),
      "composition"
    )

  def checkApplyLawForState[A, B, C](
      fa: State[String, A],
      fab: State[String, A => B],
      fbc: State[String, B => C],
      s: String
  )(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  )(using Apply[[X] =>> State[String, X]]): Unit =
    // checkFunctorLawForState[A, B, C](fa, s)
    assertEquals(
      apply(fbc)(apply(fab)(fa)).run(s),
      apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa).run(s),
      "composition"
    )
