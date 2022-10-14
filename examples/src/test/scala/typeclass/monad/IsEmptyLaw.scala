package typeclass.monad

trait IsEmptyLaw extends PlusEmptyLaw:
  def checkIsEmptyLawLaw[F[_]: IsEmpty, A](f1: F[A], f2: F[A], f3: F[A]): Unit =
    val ins = summon[IsEmpty[F]]
    checkPlusEmptyLaw(f1, f2, f3)
    assert(ins.isEmpty(ins.empty[A]))
    assertEquals(ins.isEmpty(f1) && ins.isEmpty(f2), ins.isEmpty(ins.plus(f1, f2)))
