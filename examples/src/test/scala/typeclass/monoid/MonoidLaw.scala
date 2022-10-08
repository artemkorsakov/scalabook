package typeclass.monoid

import typeclass.monoid.Monoid.{combine, empty}

trait MonoidLaw extends SemigroupLaw:
  def checkMonoidLaw[A](x: A, y: A, z: A)(using Monoid[A]): Unit =
    checkSemigroupLaw(x, y, z)
    checkRightIdentity(x)
    checkLeftIdentity(x)

  private def checkRightIdentity[A](x: A)(using Monoid[A]): Unit =
    assertEquals(combine(x, empty), x, "right identity")

  private def checkLeftIdentity[A](x: A)(using Monoid[A]): Unit =
    assertEquals(combine(empty, x), x, "left identity")
