package ru.scalabook.typeclass.monoid

import cats.data.*

trait SemiringLaw extends CMonoidLaw, MultiplicativeSemigroupLaw:
  def checkSemiringLaw[A: Semiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkCMonoidLaw(x, y, z) combine
      checkMultiplicativeSemigroupLaw(x, y, z) combine
      check(
        Semiring[A].times(x, Semiring[A].combine(y, z)) ==
          Semiring[A].combine(Semiring[A].times(x, y), Semiring[A].times(x, z)),
        "left distributivus: x * (y + z) = x * y + x * z"
      ) combine
      check(
        Semiring[A].times(Semiring[A].combine(x, y), z) ==
          Semiring[A].combine(Semiring[A].times(x, z), Semiring[A].times(y, z)),
        "right distributivus: (x + y) * z = x * z + y * z"
      ) combine
      check(
        Semiring[A].times(x, Semiring[A].empty) == Semiring[A].empty,
        "left zero multiplicative: a * 0 = 0"
      ) combine
      check(
        Semiring[A].times(Semiring[A].empty, x) == Semiring[A].empty,
        "right zero multiplicative: 0 * a = 0"
      )
end SemiringLaw
