package typeclass.monoid

import typeclass.monoid.Monoid.combine

trait GroupLaw extends MonoidLaw:
  def checkGroupLaw[A](x: A, y: A, z: A)(using g: Group[A]): Unit =
    checkMonoidLaw(x, y, z)
    assertEquals(combine(x, g.inverse(x)), g.empty, "inverse law")
