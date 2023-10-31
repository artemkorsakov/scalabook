package ru.scalabook.typeclass.monoid

trait SemiringLaw extends CMonoidLaw, MultiplicativeSemigroupLaw:
  def checkSemiringLaw[A: Semiring](x: A, y: A, z: A): Unit =
    checkCMonoidLaw(x, y, z)
    checkMultiplicativeSemigroupLaw(x, y, z)
