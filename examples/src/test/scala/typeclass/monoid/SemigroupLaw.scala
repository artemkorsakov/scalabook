package typeclass.monoid

import munit.Assertions

trait SemigroupLaw extends Assertions:
  def checkSemigroupLaw[A: Semigroup](x: A, y: A, z: A): Unit =
    assertEquals(
      Semigroup[A].combine(Semigroup[A].combine(x, y), z),
      Semigroup[A].combine(x, Semigroup[A].combine(y, z)),
      "Associativity"
    )

  def checkSemigroupLawWithRunner[A: Semigroup, B](x: A, y: A, z: A)(run: A => B): Unit =
    assertEquals(
      run(Semigroup[A].combine(Semigroup[A].combine(x, y), z)),
      run(Semigroup[A].combine(x, Semigroup[A].combine(y, z))),
      "Associativity"
    )
