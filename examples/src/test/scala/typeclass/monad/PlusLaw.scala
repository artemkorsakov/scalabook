package typeclass.monad

import munit.Assertions
import typeclass.monoid.Semigroup.combine

trait PlusLaw extends Assertions:
  def checkSemigroupLaw[A](x: A, y: A, z: A)(using Semigroup[A]): Unit =
    assertEquals(combine(combine(x, y), z), combine(x, combine(y, z)), "Associativity")
