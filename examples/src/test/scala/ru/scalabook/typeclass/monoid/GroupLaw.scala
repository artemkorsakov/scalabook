package ru.scalabook.typeclass.monoid

trait GroupLaw extends MonoidLaw, munit.Assertions:
  def checkGroupLaw[A](x: A, y: A, z: A)(using g: Group[A]): Unit =
    checkMonoidLaw(x, y, z)
    assertEquals(Group[A].combine(x, g.inverse(x)), g.empty, "inverse law 1")
    assertEquals(Group[A].combine(g.inverse(x), x), g.empty, "inverse law 2")
