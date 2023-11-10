package ru.scalabook.typeclass.monoid

trait RingWithUnityLaw extends RingLaw, SemiringWithUnityLaw:
  def checkRingWithUnityLaw[A: RingWithUnity](x: A, y: A, z: A): Unit =
    checkRingLaw(x, y, z): Unit
    checkSemiringWithUnityLaw(x, y, z)
