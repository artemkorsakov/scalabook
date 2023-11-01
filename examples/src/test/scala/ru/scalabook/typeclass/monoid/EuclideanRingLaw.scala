package ru.scalabook.typeclass.monoid

trait EuclideanRingLaw extends GCDRingLaw:
  def checkEuclideanRingLaw[A: EuclideanRing](x: A, y: A, z: A): Unit =
    checkGCDRingLaw(x, y, z)
    assertEquals(
      EuclideanRing[A].combine(
        EuclideanRing[A].times(y, EuclideanRing[A].quot(x, y)),
        EuclideanRing[A].mod(x, y)
      ),
      x,
      "b * (a /~ b) + (a % b) = a`"
    )
