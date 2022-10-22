package typeclass.monad

import typeclass.common.Runner1
import typeclass.monoid.SemigroupLaw

trait PlusLaw extends SemigroupLaw:
  def checkPlusLaw[F[_]: Plus, A](f1: F[A], f2: F[A], f3: F[A]): Unit =
    val ins = summon[Plus[F]]
    assertEquals(ins.plus(ins.plus(f1, f2), f3), ins.plus(f1, ins.plus(f2, f3)), "Associativity")
    checkSemigroupLaw(f1, f2, f3)(using ins.semigroup)

  def checkPlusLawWithRunner[F[_]: Plus: Runner1, A](f1: F[A], f2: F[A], f3: F[A]): Unit =
    checkPlusLawWithRunner[F, A](f1, f2, f3)(summon[Runner1[F]].run)

  def checkPlusLawWithRunner[F[_]: Plus, A](f1: F[A], f2: F[A], f3: F[A])(run: F[A] => A): Unit =
    val ins = summon[Plus[F]]
    import ins.plus
    assertEquals(run(plus(plus(f1, f2), f3)), run(plus(f1, plus(f2, f3))), "Associativity")
    checkSemigroupLaw(f1, f2, f3)(run)(using ins.semigroup)
