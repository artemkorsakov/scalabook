package ru.scalabook.typeclass.monoid

import cats.data.ValidatedNel

trait EuclideanRingLaw extends GCDRingLaw:
  def checkEuclideanRingLaw[A: EuclideanRing](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkGCDRingLaw(x, y, z) combine
      check(
        EuclideanRing[A].combine(
          EuclideanRing[A].times(
            y,
            EuclideanRing[A].quot(x, y)
          ),
          EuclideanRing[A].mod(x, y)
        ) == x,
        "b * (a /~ b) + (a % b) = a"
      )
