package typeclass.arrow

import typeclass.common.{Runner1, Runner2}
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
    def run[R]: A =>: R => R = summon[Runner2[=>:]].run(a)
    assertEquals(run(compose(cd, compose(bc, ab))), run(compose(compose(cd, bc), ab)), "Associativity")
    checkPlusLawWithRunner[[X] =>> X =>: X, A](f1, f2, f3)(run)(using plus)
    checkSemigroupLaw[A =>: A, A](f1, f2, f3)(run)(using semigroup)
