package ru.scalabook.typeclass.arrow

import ru.scalabook.typeclass.common.Runner2

trait ComposeLaw extends munit.Assertions:
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
    val pl = Compose[=>:].plus
    assertEquals(
      Runner2[=>:].run(a)(pl.plus(pl.plus(f1, f2), f3)),
      Runner2[=>:].run(a)(pl.plus(f1, pl.plus(f2, f3))),
      "Associativity"
    )
