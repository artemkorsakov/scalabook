package ru.scalabook.typeclass.monoid

trait MonoidLaw extends SemigroupLaw, munit.Assertions:
  def checkMonoidLaw[A](x: A, y: A, z: A)(using Monoid[A]): Unit =
    checkSemigroupLaw(x, y, z)
    assertEquals(Monoid[A].combine(x, Monoid[A].empty), x, "right identity")
    assertEquals(Monoid[A].combine(Monoid[A].empty, x), x, "left identity")
