package ru.scalabook.typeclass.monoid

trait Semiring[A] extends CMonoid[A], MultiplicativeSemigroup[A]

object Semiring:
  def apply[A: Semiring]: Semiring[A] = summon[Semiring[A]]
