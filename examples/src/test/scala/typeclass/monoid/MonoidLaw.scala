package typeclass.monoid

trait MonoidLaw extends SemigroupLaw:
  def checkMonoidLaw[A](x: A, y: A, z: A)(using Monoid[A]): Unit =
    checkSemigroupLaw(x, y, z)
    assertEquals(Monoid[A].combine(x, Monoid[A].empty), x, "right identity")
    assertEquals(Monoid[A].combine(Monoid[A].empty, x), x, "left identity")

  def checkMonoidLawWithRunner[A, B](x: A, y: A, z: A)(run: A => B)(using Monoid[A]): Unit =
    checkSemigroupLawWithRunner(x, y, z)(run)
    assertEquals(run(Monoid[A].combine(x, Monoid[A].empty)), run(x), "right identity")
    assertEquals(run(Monoid[A].combine(Monoid[A].empty, x)), run(x), "left identity")
