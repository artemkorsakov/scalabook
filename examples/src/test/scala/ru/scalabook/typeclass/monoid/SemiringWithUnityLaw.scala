package ru.scalabook.typeclass.monoid

trait SemiringWithUnityLaw extends SemiringLaw, munit.Assertions:
  def checkSemiringWithUnityLaw[A: SemiringWithUnity](x: A, y: A, z: A): Unit =
    checkSemiringLaw(x, y, z)
    assertEquals(
      SemiringWithUnity[A].times(x, SemiringWithUnity[A].one),
      x,
      "times right identity"
    )
    assertEquals(
      SemiringWithUnity[A].times(SemiringWithUnity[A].one, x),
      x,
      "times left identity"
    )
