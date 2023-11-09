package ru.scalabook.typeclass.monoid

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.Greater

import scala.annotation.tailrec

trait MultiplicativeSemigroup[A]:
  def times(x: A, y: A): A

  def pow(a: A, n: Int :| Greater[0]): A =
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
