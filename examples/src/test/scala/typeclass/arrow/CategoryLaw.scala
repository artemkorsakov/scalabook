package typeclass.arrow

import typeclass.common.Runner2
import typeclass.common.Runner2.run
import typeclass.monad.PlusEmptyLaw

trait CategoryLaw extends ComposeLaw, PlusEmptyLaw:
  def checkCategoryLaw[=>:[_, _]: Category: Runner2, A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A,
      f2: A =>: A,
      f3: A =>: A
  )(a: A): Unit =
    val ins = summon[Category[=>:]]
    import ins.{compose, id}
    checkComposeLaw[=>:, A, B, C, D](ab, bc, cd, f1, f2, f3)(a)
    checkPlusEmptyLawWithRunner[[A] =>> A =>: A, A](f1, f2, f3)(run(a))(using ins.empty)
    checkMonoidLawWithRunner[A =>: A, A](f1, f2, f3)(run(a))(using ins.monoid)
    assertEquals(run(compose(f1, id[A])), run(f1), "right identity")
    assertEquals(run(compose(id[A], f1)), run(f1), "left identity")
