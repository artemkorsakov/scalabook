package typeclass.monad

import typeclass.common.Runner1
import typeclass.monoid.SemigroupLaw

trait PlusLaw extends SemigroupLaw:
  def checkPlusLaw[F[_]: Plus, A](f1: F[A], f2: F[A], f3: F[A]): Unit =
    assertEquals(Plus[F].plus(Plus[F].plus(f1, f2), f3), Plus[F].plus(f1, Plus[F].plus(f2, f3)), "Associativity")
    checkSemigroupLaw(f1, f2, f3)(using Plus[F].semigroup)

  def checkPlusLawWithRunner[F[_]: Plus: Runner1, A](f1: F[A], f2: F[A], f3: F[A]): Unit =
    checkPlusLawWithRunner[F, A](f1, f2, f3)(Runner1[F].run)

  def checkPlusLawWithRunner[F[_]: Plus, A](f1: F[A], f2: F[A], f3: F[A])(run: F[A] => A): Unit =
    assertEquals(
      run(Plus[F].plus(Plus[F].plus(f1, f2), f3)),
      run(Plus[F].plus(f1, Plus[F].plus(f2, f3))),
      "Associativity"
    )
    checkSemigroupLawWithRunner(f1, f2, f3)(run)(using Plus[F].semigroup)
