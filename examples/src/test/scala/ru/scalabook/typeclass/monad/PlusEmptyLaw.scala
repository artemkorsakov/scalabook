package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel
import ru.scalabook.typeclass.monoid.MonoidLaw

trait PlusEmptyLaw extends PlusLaw, MonoidLaw:
  def checkPlusEmptyLaw[F[_]: PlusEmpty, A](
      f1: F[A],
      f2: F[A],
      f3: F[A]
  ): ValidatedNel[String, Unit] =
    checkPlusLaw(f1, f2, f3) combine
      check(
        PlusEmpty[F].plus(f1, PlusEmpty[F].empty) ==
          f1,
        "right identity"
      ) combine
      check(
        PlusEmpty[F].plus(PlusEmpty[F].empty, f1) == f1,
        "left identity"
      ) combine
      checkMonoidLaw(f1, f2, f3)(using PlusEmpty[F].monoid)
