package ru.scalabook.typeclass.monoid

import cats.data.ValidatedNel

trait SemiringWithUnityLaw extends SemiringLaw:
  def checkSemiringWithUnityLaw[A: SemiringWithUnity](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemiringLaw(x, y, z) combine
      check(
        SemiringWithUnity[A].times(x, SemiringWithUnity[A].one) == x,
        "times right identity: x * 1 = x"
      ) combine
      check(
        SemiringWithUnity[A].times(SemiringWithUnity[A].one, x) == x,
        "times left identity: 1 * x = x"
      )
