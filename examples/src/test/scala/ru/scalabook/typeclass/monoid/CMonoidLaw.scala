package ru.scalabook.typeclass.monoid

trait CMonoidLaw extends MonoidLaw, CSemigroupLaw:
  def checkCMonoidLaw[A: CMonoid](x: A, y: A, z: A): Unit =
    checkMonoidLaw(x, y, z)
    checkCSemigroupLaw(x, y, z)
