package ru.scalabook.blog.architectureproblems

import org.scalacheck.Gen
import ru.scalabook.blog.architectureproblems.Semigroup.doTheSemigroupLawsHold
import weaver.*
import weaver.scalacheck.*

object SemigroupSuite extends SimpleIOSuite with Checkers:
  private val smallNumber = Gen.choose[Int](-100, 100)
  private val smallNumbers =
    for
      x <- smallNumber
      y <- smallNumber
      z <- smallNumber
    yield (x, y, z)

  test("A set of numbers forms a semigroup under multiplication"):
    given Semigroup[Int] with
      extension (x: Int) override def |+|(y: Int): Int = x * y

    forall(smallNumbers): (x, y, z) =>
      expect.all(doTheSemigroupLawsHold(x, y, z))

  pureTest("A set of numbers does not form a semigroup under subtraction"):
    given Semigroup[Int] with
      extension (x: Int) override def |+|(y: Int): Int = x - y

    expect.all(!doTheSemigroupLawsHold(1, 2, 3))
