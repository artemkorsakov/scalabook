package ru.scalabook.blog.architectureproblems

import org.scalacheck.Gen
import ru.scalabook.blog.architectureproblems.Monoid.doTheMonoidLawsHold
import weaver.*
import weaver.scalacheck.*

object MonoidSuite extends SimpleIOSuite with Checkers:
  private val smallNumber = Gen.choose[Int](-100, 100)
  private val smallNumbers =
    for
      x <- smallNumber
      y <- smallNumber
      z <- smallNumber
    yield (x, y, z)

  test(
    "The set of numbers forms a monoid under multiplication with the identity element equal to 1"
  ):
    given Monoid[Int] with
      override def e: Int                              = 1
      extension (x: Int) override def |+|(y: Int): Int = x * y

    forall(smallNumbers): (x, y, z) =>
      expect.all(doTheMonoidLawsHold(x, y, z))

  pureTest(
    "The set of numbers does not form a monoid under multiplication with the unit element equal to 0"
  ):
    given Monoid[Int] with
      override def e: Int                              = 0
      extension (x: Int) override def |+|(y: Int): Int = x * y

    expect.all(!doTheMonoidLawsHold(1, 2, 3))
end MonoidSuite
