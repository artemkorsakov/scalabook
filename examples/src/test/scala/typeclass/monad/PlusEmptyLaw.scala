package typeclass.monad

import typeclass.monoid.MonoidLaw

trait PlusEmptyLaw extends PlusLaw, MonoidLaw:
  def checkPlusEmptyLaw[F[_], A](f1: F[A], f2: F[A], f3: F[A])(using p: PlusEmpty[F]): Unit =
    checkPlusLaw(f1, f2, f3)
    assertEquals(p.plus(f1, p.empty), f1, "right identity")
    assertEquals(p.plus(p.empty, f1), f1, "left identity")
    checkMonoidLaw(f1, f2, f3)(using p.monoid)
