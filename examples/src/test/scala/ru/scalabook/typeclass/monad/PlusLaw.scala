package ru.scalabook.typeclass.monad

import cats.data.ValidatedNel
import ru.scalabook.typeclass.common.OldLaws

trait PlusLaw extends OldLaws:
  def checkPlusLaw[F[_]: Plus, A](
      f1: F[A],
      f2: F[A],
      f3: F[A]
  ): ValidatedNel[String, Unit] =
    check(
      Plus[F].plus(Plus[F].plus(f1, f2), f3) ==
        Plus[F].plus(f1, Plus[F].plus(f2, f3)),
      "Associativity"
    ) combine
      checkSemigroupLaw(f1, f2, f3)(using Plus[F].semigroup)
