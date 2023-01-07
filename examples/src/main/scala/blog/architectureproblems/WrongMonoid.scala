package blog.architectureproblems

import cats.data.NonEmptyList

trait WrongMonoid[A]:
  def e: A
  extension (x: A) def |+|(y: A): A

object WrongMonoid:
  given WrongMonoid[NonEmptyList[Int]] with
    override def e: NonEmptyList[Int]                           = ???
    extension (x: NonEmptyList[Int])
      override def |+|(y: NonEmptyList[Int]): NonEmptyList[Int] =
        x ++ y
