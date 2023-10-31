package ru.scalabook.typeclass.monoid

trait Semiring[A] extends CMonoid[A], MultiplicativeSemigroup[A]

object Semiring:
  def apply[A: Semiring]: Semiring[A] = summon[Semiring[A]]

  given Semiring[Int] with
    val empty                        = 0
    def combine(x: Int, y: Int): Int = x + y
    def times(x: Int, y: Int): Int   = x * y
