package ru.scalabook.typeclass.monoid

trait CSemigroupLaw extends SemigroupLaw:
  def checkCSemigroupLaw[A](x: A, y: A, z: A)(using cs: CSemigroup[A]): Unit =
    checkSemigroupLaw(x, y, z)
    assertEquals(cs.combine(x, y), cs.combine(y, x), "commutative")
