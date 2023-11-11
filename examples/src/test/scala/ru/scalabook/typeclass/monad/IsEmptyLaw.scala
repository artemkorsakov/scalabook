package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel

trait IsEmptyLaw extends PlusEmptyLaw:
  def checkIsEmptyLawLaw[F[_]: IsEmpty, A](
      f1: F[A],
      f2: F[A],
      f3: F[A]
  ): ValidatedNel[String, Unit] =
    checkPlusEmptyLaw(f1, f2, f3) combine
      check(
        IsEmpty[F].isEmpty(IsEmpty[F].empty[A]),
        "isEmpty(empty) - true"
      ) combine
      check(
        (IsEmpty[F].isEmpty(f1) && IsEmpty[F].isEmpty(f2)) ==
          IsEmpty[F].isEmpty(IsEmpty[F].plus(f1, f2)),
        "isEmpty(f1) && isEmpty(f2) = isEmpty(f1 + f2)"
      )
