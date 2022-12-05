package typeclass.arrow

import munit.Assertions
import typeclass.common.Runner2

trait ProfunctorLaw extends Assertions:
  def checkProfunctorLaw[=>:[_, _]: Profunctor: Runner2, A, B, C, D, E, F](
      gad: A =>: D
  )(a: A, c: C)(using fcb: C => B, fba: B => A, fde: D => E, fef: E => F): Unit =
    val ins = summon[Profunctor[=>:]]
    import ins.dimap
    assertEquals(Runner2[=>:].run(a)(dimap(gad)((a: A) => a)((d: D) => d)), Runner2[=>:].run(a)(gad), "identity")
    assertEquals(
      Runner2[=>:].run(c)(dimap(dimap(gad)(fba)(fde))(fcb)(fef)),
      Runner2[=>:].run(c)(dimap(gad)(fba compose fcb)(fef compose fde)),
      "composite"
    )
