package typeclass.arrow

import typeclass.common.Runner2
import typeclass.common.Runner2.run
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
    val ins = summon[Compose[=>:]]
    import ins.{compose, plus, semigroup}
    assertEquals(run(a)(compose(cd, compose(bc, ab))), run(a)(compose(compose(cd, bc), ab)), "Associativity")
    checkPlusLawWithRunner[[X] =>> X =>: X, A](f1, f2, f3)(run(a))(using plus)
    checkSemigroupLawWithRunner[A =>: A, A](f1, f2, f3)(run(a))(using semigroup)
