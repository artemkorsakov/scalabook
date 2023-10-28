package ru.scalabook.typeclass.monoid

trait Semigroup[A]:
  def combine(x: A, y: A): A

  def combineN(x: A, n: Int): Option[A] =
    if n <= 0 then None
    else if n == 1 then Some(x)
    else combineN(x, n - 1).map(y => combine(x, y))

object Semigroup:
  def apply[A: Semigroup]: Semigroup[A] = summon[Semigroup[A]]

  given sumSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x + y

  given productSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x * y

  given stringSemigroupInstance: Semigroup[String] = (x: String, y: String) =>
    x + y

  given listSemigroupInstance[T]: Semigroup[List[T]] =
    (x: List[T], y: List[T]) => x ++ y

  given nestedSemigroupInstance[A, B](using
      aSemigroup: Semigroup[A],
      bSemigroup: Semigroup[B]
  ): Semigroup[(A, B)] =
    (x: (A, B), y: (A, B)) =>
      (aSemigroup.combine(x._1, y._1), bSemigroup.combine(x._2, y._2))
