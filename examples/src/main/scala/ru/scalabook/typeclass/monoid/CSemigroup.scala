package ru.scalabook.typeclass.monoid

trait CSemigroup[A] extends Semigroup[A]

object CSemigroup:
  given CSemigroup[Int] = (x: Int, y: Int) => x + y
