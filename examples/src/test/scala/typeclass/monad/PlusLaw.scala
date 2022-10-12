package typeclass.monad

import typeclass.monoid.SemigroupLaw

trait PlusLaw extends SemigroupLaw:
  def checkPlusLaw[F[_], A](f1: F[A], f2: F[A], f3: F[A])(using p: Plus[F]): Unit =
    assertEquals(p.plus(p.plus(f1, f2), f3), p.plus(f1, p.plus(f2, f3)), "Associativity")
    checkSemigroupLaw[F[A]](f1: F[A], f2: F[A], f3: F[A])(using p.semigroup)
