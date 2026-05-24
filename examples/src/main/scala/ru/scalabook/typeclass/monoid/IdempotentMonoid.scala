package ru.scalabook.typeclass.monoid

trait IdempotentMonoid[A] extends Monoid[A], Band[A]
