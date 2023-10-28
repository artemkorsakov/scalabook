package ru.scalabook.typeclass.monoid

trait CMonoid[A] extends Monoid[A], CSemigroup[A]

object CMonoid:
  def apply[A: CMonoid]: CMonoid[A] = summon[CMonoid[A]]

  given CMonoid[Int] with
    val empty                        = 0
    def combine(x: Int, y: Int): Int = x + y
