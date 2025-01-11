package ru.scalabook.typeclass.monoid

trait IdempotentMonoid[A] extends Monoid[A], Band[A]

object IdempotentMonoid:
  given [A]: IdempotentMonoid[Set[A]] with
    def empty: Set[A] = Set.empty[A]

    def combine(x: Set[A], y: Set[A]): Set[A] = x ++ y
