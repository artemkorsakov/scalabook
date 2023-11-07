package ru.scalabook.typeclass.monoid

import io.github.iltotore.iron.:|
import io.github.iltotore.iron.constraint.all.Greater

trait Band[A] extends Semigroup[A]:
  override def combineN(x: A, n: Int :| Greater[0]): A = x

object Band:
  def apply[A: Band]: Band[A] = summon[Band[A]]

  given [A]: Band[Set[A]] =
    (x: Set[A], y: Set[A]) => x ++ y
