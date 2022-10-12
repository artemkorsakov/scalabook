package typeclass.monad

import munit.Assertions
import typeclass.monad.Plus.plus

trait PlusLaw extends Assertions:
  def checkPlusLaw[F[_], A](f1: F[A], f2: F[A], f3: F[A])(using Plus[F]): Unit =
    assertEquals(plus(plus(f1, f2), f3), plus(f1, plus(f2, f3)), "Associativity")
