package typeclass.arrow

import typeclass.monad.{ContravariantFunctorLaw, FunctorLaw}

trait ProfunctorLaw extends FunctorLaw, ContravariantFunctorLaw:
  def checkProfunctorLaw[=>:[_, _]: Profunctor, A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A,
      f2: A =>: A,
      f3: A =>: A
  )(runAA: A =>: A => A, runAD: A =>: D => D, run: (A =>: A) | (B =>: B) | (C =>: C) => A | B | C)(using
      f: A => B,
      fReverse: B => A,
      g: B => C,
      gReverse: C => B
  ): Unit =
    val ins = summon[Profunctor[=>:]]
    assertEquals(42, 42)
    checkInvariantFunctorLaw[[X] =>> X =>: X, A, B, C](f1, run)(using ins.invariantFunctor, f, fReverse, g, gReverse)
    checkFunctorLaw[[X] =>> A =>: X, A, B, C](f1, runAA)(using ins.covariantInstance[A], f, fReverse, g, gReverse)
    checkContravariantFunctorLaw()
    ???
