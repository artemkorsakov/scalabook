package typeclass.monoid

import typeclass.monoid.Monoid.{combine, empty}

trait MonoidLaw extends SemigroupLaw:
  def checkMonoidLaw[A](x: A, y: A, z: A)(using Monoid[A]): Unit =
    checkSemigroupLaw(x, y, z)
    assertEquals(combine(x, empty), x, "right identity")
    assertEquals(combine(empty, x), x, "left identity")

  def checkMonoidLaw[A, B](x: A, y: A, z: A)(run: A => B)(using Monoid[A]): Unit =
    checkSemigroupLaw(x, y, z)(run)
    assertEquals(run(combine(x, empty)), run(x), "right identity")
    assertEquals(run(combine(empty, x)), run(x), "left identity")
