package ru.scalabook.typeclass.monoid

trait CSemigroup[A] extends Semigroup[A]

object CSemigroup:
  def apply[A: CSemigroup]: CSemigroup[A] = summon[CSemigroup[A]]

  given CSemigroup[Int] = (x: Int, y: Int) => x + y
