package typeclass.arrow

import typeclass.common.Runner2
import typeclass.monad.PlusLaw

trait ComposeLaw extends PlusLaw:
  def checkComposeLaw[=>:[_, _]: Compose: Runner2, A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A,
      f2: A =>: A,
      f3: A =>: A
  )(a: A): Unit =
    assertEquals(
      Runner2[=>:].run(a)(
        Compose[=>:].compose(cd, Compose[=>:].compose(bc, ab))
      ),
      Runner2[=>:].run(a)(
        Compose[=>:].compose(Compose[=>:].compose(cd, bc), ab)
      ),
      "Associativity"
    )
    checkPlusLawWithRunner[[X] =>> X =>: X, A](f1, f2, f3)(Runner2[=>:].run(a))(
      using Compose[=>:].plus
    )
    checkSemigroupLawWithRunner[A =>: A, A](f1, f2, f3)(Runner2[=>:].run(a))(
      using Compose[=>:].semigroup
    )
