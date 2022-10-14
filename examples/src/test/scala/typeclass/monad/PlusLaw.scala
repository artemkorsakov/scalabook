package typeclass.monad

import typeclass.monoid.SemigroupLaw

trait PlusLaw extends SemigroupLaw:
  def checkPlusLaw[F[_]: Plus, A](f1: F[A], f2: F[A], f3: F[A]): Unit =
    val ins = summon[Plus[F]]
    assertEquals(ins.plus(ins.plus(f1, f2), f3), ins.plus(f1, ins.plus(f2, f3)), "Associativity")
    checkSemigroupLaw(f1, f2, f3)(using ins.semigroup)
