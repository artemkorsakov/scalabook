package ru.scalabook.typeclass.monoid

trait Field[A] extends EuclideanRing[A]:
  extension (a: A) def reciprocal: A

  def div(x: A, y: A): A = times(x, y.reciprocal)

object Field:
  def apply[A: Field]: Field[A] = summon[Field[A]]
