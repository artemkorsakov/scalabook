package typeclass.arrow

import munit.Assertions

trait ProfunctorLaw extends Assertions:
  def checkProfunctorLaw[=>:[_, _]: Profunctor, A, B, C, D, E, F](
      gad: A =>: D
  )(runAD: A =>: D => D, runCF: C =>: F => F)(using fcb: C => B, fba: B => A, fde: D => E, fef: E => F): Unit =
    val ins = summon[Profunctor[=>:]]
    import ins.dimap
    assertEquals(runAD(dimap(gad)((a: A) => a)((d: D) => d)), runAD(gad), "identity")
    assertEquals(
      runCF(dimap(dimap(gad)(fba)(fde))(fcb)(fef)),
      runCF(dimap(gad)(fba compose fcb)(fef compose fde)),
      "composite"
    )
