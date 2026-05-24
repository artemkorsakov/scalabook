package ru.scalabook.typeclass.monoid

trait ISemiring[A] extends Semiring[A], IdempotentMonoid[A]

object ISemiring:
  def apply[A: ISemiring]: ISemiring[A] = summon[ISemiring[A]]
