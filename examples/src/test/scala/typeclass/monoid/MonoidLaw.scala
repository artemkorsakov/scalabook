package typeclass.monoid

import typeclass.monoid.Monoid.{combine, empty}

trait MonoidLaw extends SemigroupLaw:
  def checkMonoidLaw[A](x: A, y: A, z: A)(using Monoid[A]): Unit =
    checkSemigroupLaw(x, y, z)
    assertEquals(combine(x, empty), x, "right identity")
    assertEquals(combine(empty, x), x, "left identity")
