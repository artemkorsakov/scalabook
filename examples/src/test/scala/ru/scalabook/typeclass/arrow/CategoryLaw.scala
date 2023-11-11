package ru.scalabook.typeclass.arrow

import ru.scalabook.typeclass.common.Runner2

trait CategoryLaw extends ComposeLaw:
  def checkCategoryLaw[=>:[_, _]: Category: Runner2, A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A,
      f2: A =>: A,
      f3: A =>: A
  )(a: A): Unit =
    checkComposeLaw[=>:, A, B, C, D](ab, bc, cd, f1, f2, f3)(a)
    val ple = Category[=>:].empty
    assertEquals(
      Runner2[=>:].run(a)(ple.plus(f1, ple.empty)),
      Runner2[=>:].run(a)(f1),
      "right identity"
    )
    assertEquals(
      Runner2[=>:].run(a)(ple.plus(ple.empty, f1)),
      Runner2[=>:].run(a)(f1),
      "left identity"
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
