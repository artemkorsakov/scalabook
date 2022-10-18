package typeclass.arrow

import typeclass.monad.PlusLaw

trait ComposeLaw extends PlusLaw:
  def checkComposeLaw[=>:[_, _]: Compose, A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A,
      f2: A =>: A,
      f3: A =>: A
  )(runAA: A =>: A => A, runAD: A =>: D => D): Unit =
    val ins = summon[Compose[=>:]]
    import ins.compose
    assertEquals(runAD(compose(cd, compose(bc, ab))), runAD(compose(compose(cd, bc), ab)), "Associativity")
    checkPlusLaw[[A] =>> A =>: A, A](f1, f2, f3)(runAA)(using ins.plus)
    checkSemigroupLaw[A =>: A, A](f1, f2, f3)(runAA)(using ins.semigroup)
