package typeclass.arrow

import typeclass.common.Runner2
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
    checkComposeLaw[=>:, A, B, C, D](ab, bc, cd, f1, f2, f3)(a)
    checkPlusEmptyLawWithRunner[[A] =>> A =>: A, A](f1, f2, f3)(
      Runner2[=>:].run(a)
    )(using Category[=>:].empty)
    checkMonoidLawWithRunner[A =>: A, A](f1, f2, f3)(Runner2[=>:].run(a))(using
      Category[=>:].monoid
    )
    assertEquals(
      Runner2[=>:].run(a)(Category[=>:].compose(f1, Category[=>:].id[A])),
      Runner2[=>:].run(a)(f1),
      "right identity"
    )
    assertEquals(
      Runner2[=>:].run(a)(Category[=>:].compose(Category[=>:].id[A], f1)),
      Runner2[=>:].run(a)(f1),
      "left identity"
    )
