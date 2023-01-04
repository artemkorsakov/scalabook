package typeclass.monad

trait IsEmptyLaw extends PlusEmptyLaw:
  def checkIsEmptyLawLaw[F[_]: IsEmpty, A](f1: F[A], f2: F[A], f3: F[A]): Unit =
    checkPlusEmptyLaw(f1, f2, f3)
    assert(IsEmpty[F].isEmpty(IsEmpty[F].empty[A]))
    assertEquals(
      IsEmpty[F].isEmpty(f1) && IsEmpty[F].isEmpty(f2),
      IsEmpty[F].isEmpty(IsEmpty[F].plus(f1, f2))
    )
