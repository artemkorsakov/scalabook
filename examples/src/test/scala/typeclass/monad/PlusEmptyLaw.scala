package typeclass.monad

import typeclass.monoid.MonoidLaw

trait PlusEmptyLaw extends PlusLaw, MonoidLaw:
  def checkPlusEmptyLaw[F[_]: PlusEmpty, A](f1: F[A], f2: F[A], f3: F[A]): Unit =
    val ins = summon[PlusEmpty[F]]
    checkPlusLaw(f1, f2, f3)
    assertEquals(ins.plus(f1, ins.empty), f1, "right identity")
    assertEquals(ins.plus(ins.empty, f1), f1, "left identity")
    checkMonoidLaw(f1, f2, f3)(using ins.monoid)
