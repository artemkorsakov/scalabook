package ru.scalabook.typeclass.monoid

trait AbGroupLaw extends GroupLaw, CMonoidLaw:
  def checkAbGroupLaw[A: AbGroup](x: A, y: A, z: A): Unit =
    checkGroupLaw(x, y, z)
    checkCMonoidLaw(x, y, z)
