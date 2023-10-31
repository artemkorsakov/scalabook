package ru.scalabook.typeclass.monoid

trait RingLaw extends AbGroupLaw, SemiringLaw:
  def checkRingLaw[A: Ring](x: A, y: A, z: A): Unit =
    checkAbGroupLaw(x, y, z)
    checkSemiringLaw(x, y, z)
