package typeclass.arrow

import typeclass.monad.PlusEmptyLaw

trait CategoryLaw extends ComposeLaw, PlusEmptyLaw:
  def checkCategoryLaw[=>:[_, _]: Category, A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A,
      f2: A =>: A,
      f3: A =>: A
  )(runAA: A =>: A => A, runAD: A =>: D => D): Unit =
    val ins = summon[Category[=>:]]
    import ins.{compose, id}
    checkComposeLaw[=>:, A, B, C, D](ab, bc, cd, f1, f2, f3)(runAA, runAD)
    checkPlusEmptyLaw[[A] =>> A =>: A, A](f1, f2, f3)(runAA)(using ins.empty)
    checkMonoidLaw[A =>: A, A](f1, f2, f3)(runAA)(using ins.monoid)
    assertEquals(runAA(compose(f1, id[A])), runAA(f1), "right identity")
    assertEquals(runAA(compose(id[A], f1)), runAA(f1), "left identity")
