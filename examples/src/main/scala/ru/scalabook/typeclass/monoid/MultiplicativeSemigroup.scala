package ru.scalabook.typeclass.monoid

trait MultiplicativeSemigroup[A]:
  def times(x: A, y: A): A

object MultiplicativeSemigroup:
  def apply[A: MultiplicativeSemigroup]: MultiplicativeSemigroup[A] =
    summon[MultiplicativeSemigroup[A]]

  given MultiplicativeSemigroup[Int] = (x: Int, y: Int) => x * y
