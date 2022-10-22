package typeclass.arrow

import munit.Assertions
import typeclass.common.Runner2
import typeclass.common.Runner2.run

trait ProfunctorLaw extends Assertions:
  def checkProfunctorLaw[=>:[_, _]: Profunctor: Runner2, A, B, C, D, E, F](
      gad: A =>: D
  )(a: A, c: C)(using fcb: C => B, fba: B => A, fde: D => E, fef: E => F): Unit =
    val ins = summon[Profunctor[=>:]]
    import ins.dimap
    assertEquals(run(a)(dimap(gad)((a: A) => a)((d: D) => d)), run(a)(gad), "identity")
    assertEquals(
      run(c)(dimap(dimap(gad)(fba)(fde))(fcb)(fef)),
      run(c)(dimap(gad)(fba compose fcb)(fef compose fde)),
      "composite"
    )
