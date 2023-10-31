package ru.scalabook.typeclass.monoid

import scala.annotation.tailrec

trait MultiplicativeSemigroup[A]:
  def times(x: A, y: A): A

  def pow(x: A, n: Int): Option[A] =
    if n <= 0 then None
    else Some(positivePow(x, n))

  private def positivePow(a: A, n: Int): A =
    @tailrec def loop(b: A, k: Int, extra: A): A =
      if k == 1 then times(b, extra)
      else
        val x = if (k & 1) == 1 then times(b, extra) else extra
        loop(times(b, b), k >>> 1, x)

    if n == 1 then a else loop(a, n - 1, a)

object MultiplicativeSemigroup:
  def apply[A: MultiplicativeSemigroup]: MultiplicativeSemigroup[A] =
    summon[MultiplicativeSemigroup[A]]

  given MultiplicativeSemigroup[Int] = (x: Int, y: Int) => x * y
