package ru.scalabook.typeclass.monoid

trait ISemiring[A] extends Semiring[A], IdempotentMonoid[A]

object ISemiring:
  def apply[A: ISemiring]: ISemiring[A] = summon[ISemiring[A]]

  given [A]: ISemiring[Set[A]] with
    val empty: Set[A]                         = Set.empty[A]
    def combine(x: Set[A], y: Set[A]): Set[A] = x ++ y
    def times(x: Set[A], y: Set[A]): Set[A]   = x.intersect(y)
