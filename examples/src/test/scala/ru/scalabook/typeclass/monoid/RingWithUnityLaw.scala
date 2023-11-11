package ru.scalabook.typeclass.monoid

import cats.data.ValidatedNel

trait RingWithUnityLaw extends RingLaw, SemiringWithUnityLaw:
  def checkRingWithUnityLaw[A: RingWithUnity](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkRingLaw(x, y, z) combine checkSemiringWithUnityLaw(x, y, z)
