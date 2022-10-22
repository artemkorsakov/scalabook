package typeclass.monad

import typeclass.monoid.MonoidLaw

trait PlusEmptyLaw extends PlusLaw, MonoidLaw:
  def checkPlusEmptyLaw[F[_]: PlusEmpty, A](f1: F[A], f2: F[A], f3: F[A]): Unit =
    val ins = summon[PlusEmpty[F]]
    checkPlusLaw(f1, f2, f3)
    assertEquals(ins.plus(f1, ins.empty), f1, "right identity")
    assertEquals(ins.plus(ins.empty, f1), f1, "left identity")
    checkMonoidLaw(f1, f2, f3)(using ins.monoid)

  def checkPlusEmptyLaw[F[_] : PlusEmpty, A](f1: F[A], f2: F[A], f3: F[A])(run: F[A] => A): Unit =
    val ins = summon[PlusEmpty[F]]
    import ins.{plus, empty}
    //checkPlusLaw(f1, f2, f3)(run)
    assertEquals(run(plus(f1, empty)), run(f1), "right identity")
    assertEquals(run(plus(empty, f1)), run(f1), "left identity")
    checkMonoidLaw(f1, f2, f3)(run)(using ins.monoid)  
