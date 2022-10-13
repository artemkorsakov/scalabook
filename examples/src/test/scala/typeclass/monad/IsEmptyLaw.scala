package typeclass.monad

trait IsEmptyLaw extends PlusEmptyLaw:
  def checkIsEmptyLawLaw[F[_], A](f1: F[A], f2: F[A], f3: F[A])(using ins: IsEmpty[F]): Unit =
    checkPlusEmptyLaw(f1, f2, f3)
    assert(ins.isEmpty(ins.empty[A]))
    assertEquals(ins.isEmpty(f1) && ins.isEmpty(f2), ins.isEmpty(ins.plus(f1, f2)))
