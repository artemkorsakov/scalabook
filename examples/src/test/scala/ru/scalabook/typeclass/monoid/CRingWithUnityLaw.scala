package ru.scalabook.typeclass.monoid

trait CRingWithUnityLaw extends RingWithUnityLaw, CRingLaw:
  def checkCRingWithUnityLaw[A: CRingWithUnity](x: A, y: A, z: A): Unit =
    checkRingWithUnityLaw(x, y, z)
    checkCRingLaw(x, y, z)
