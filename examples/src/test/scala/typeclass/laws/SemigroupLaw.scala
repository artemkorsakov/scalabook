package typeclass.laws

import munit.Assertions
import typeclass.monoid.Semigroup
import typeclass.monoid.Semigroup.combine

trait SemigroupLaw extends Assertions:
  def checkSemigroupLaw[A](x: A, y: A, z: A)(using Semigroup[A]): Unit =
    checkAssociativity(x, y, z)

  private def checkAssociativity[A](x: A, y: A, z: A)(using Semigroup[A]): Unit =
    assertEquals(combine(combine(x, y), z), combine(x, combine(y, z)), "Associativity")
