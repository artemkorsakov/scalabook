package ru.scalabook.typeclass.monoid

import cats.data.ValidatedNel

trait CRingWithUnityLaw extends RingWithUnityLaw, CRingLaw:
  def checkCRingWithUnityLaw[A: CRingWithUnity](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkRingWithUnityLaw(x, y, z) combine
      checkCRingLaw(x, y, z)
