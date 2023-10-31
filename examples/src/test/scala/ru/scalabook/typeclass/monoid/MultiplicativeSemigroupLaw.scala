package ru.scalabook.typeclass.monoid

import munit.Assertions

trait MultiplicativeSemigroupLaw extends Assertions:
  def checkMultiplicativeSemigroupLaw[A: MultiplicativeSemigroup](
      x: A,
      y: A,
      z: A
  ): Unit =
    assertEquals(
      MultiplicativeSemigroup[A]
        .times(MultiplicativeSemigroup[A].times(x, y), z),
      MultiplicativeSemigroup[A]
        .times(x, MultiplicativeSemigroup[A].times(y, z)),
      "Associativity"
    )
