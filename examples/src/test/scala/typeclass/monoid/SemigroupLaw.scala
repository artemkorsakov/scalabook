package typeclass.monoid

import munit.Assertions
import typeclass.monoid.Semigroup.combine

trait SemigroupLaw extends Assertions:
  def checkSemigroupLaw[A: Semigroup](x: A, y: A, z: A): Unit =
    assertEquals(combine(combine(x, y), z), combine(x, combine(y, z)), "Associativity")

  def checkSemigroupLaw[A: Semigroup, B](x: A, y: A, z: A)(run: A => B): Unit =
    assertEquals(run(combine(combine(x, y), z)), run(combine(x, combine(y, z))), "Associativity")
