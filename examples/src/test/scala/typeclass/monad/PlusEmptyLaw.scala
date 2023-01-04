package typeclass.monad

import typeclass.common.Runner1
import typeclass.monoid.MonoidLaw

trait PlusEmptyLaw extends PlusLaw, MonoidLaw:
  def checkPlusEmptyLaw[F[_]: PlusEmpty, A](
      f1: F[A],
      f2: F[A],
      f3: F[A]
  ): Unit =
    checkPlusLaw(f1, f2, f3)
    assertEquals(
      PlusEmpty[F].plus(f1, PlusEmpty[F].empty),
      f1,
      "right identity"
    )
    assertEquals(PlusEmpty[F].plus(PlusEmpty[F].empty, f1), f1, "left identity")
    checkMonoidLaw(f1, f2, f3)(using PlusEmpty[F].monoid)

  def checkPlusEmptyLawWithRunner[F[_]: PlusEmpty: Runner1, A](
      f1: F[A],
      f2: F[A],
      f3: F[A]
  ): Unit =
    checkPlusEmptyLawWithRunner[F, A](f1, f2, f3)(Runner1[F].run)

  def checkPlusEmptyLawWithRunner[F[_]: PlusEmpty, A](
      f1: F[A],
      f2: F[A],
      f3: F[A]
  )(run: F[A] => A): Unit =
    checkPlusLawWithRunner(f1, f2, f3)(run)
    assertEquals(
      run(PlusEmpty[F].plus(f1, PlusEmpty[F].empty)),
      run(f1),
      "right identity"
    )
    assertEquals(
      run(PlusEmpty[F].plus(PlusEmpty[F].empty, f1)),
      run(f1),
      "left identity"
    )
    checkMonoidLawWithRunner(f1, f2, f3)(run)(using PlusEmpty[F].monoid)
