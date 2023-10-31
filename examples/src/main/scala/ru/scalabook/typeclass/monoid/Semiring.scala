package ru.scalabook.typeclass.monoid

trait Semiring[A] extends CMonoid[A], MultiplicativeSemigroup[A]

object Semiring
