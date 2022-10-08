package typeclass.monoid

import typeclass.monoid.Band.combine

trait BandLaw extends SemigroupLaw:
  def checkBandLaw[A](x: A, y: A, z: A)(using Band[A]): Unit =
    checkSemigroupLaw(x, y, z)
    assertEquals(combine(x, x), x, "idempotency")
