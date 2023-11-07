package ru.scalabook.typeclass.monoid

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.Greater

import scala.annotation.tailrec

trait Semigroup[A]:
  def combine(x: A, y: A): A

  def combineN(x: A, n: Int :| Greater[0]): A =
    @tailrec def loop(b: A, k: Int, acc: A): A =
      if k == 1 then combine(b, acc)
      else
        val x = if (k & 1) == 1 then combine(b, acc) else acc
        loop(combine(b, b), k >>> 1, x)

    if n == 1 then x else loop(x, n - 1, x)

object Semigroup:
  def apply[A: Semigroup]: Semigroup[A] = summon[Semigroup[A]]

  given Semigroup[Int] = (x: Int, y: Int) => x + y
