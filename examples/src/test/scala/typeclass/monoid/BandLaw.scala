package typeclass.monoid

trait BandLaw extends SemigroupLaw:
  def checkBandLaw[A](x: A, y: A, z: A)(using Band[A]): Unit =
    checkSemigroupLaw(x, y, z)
    assertEquals(Band[A].combine(x, x), x, "idempotency")
