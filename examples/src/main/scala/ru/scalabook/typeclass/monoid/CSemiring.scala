package ru.scalabook.typeclass.monoid

trait CSemiring[A] extends Semiring[A]

object CSemiring:
  def apply[A: CSemiring]: CSemiring[A] = summon[CSemiring[A]]

  given CSemiring[Int] with
    val empty: Int                   = 0
    def combine(x: Int, y: Int): Int = x + y
    def times(x: Int, y: Int): Int   = x * y
