package ru.scalabook.typeclass.monoid

trait SemiringWithUnity[A] extends Semiring[A]:
  def one: A

object SemiringWithUnity:
  def apply[A: SemiringWithUnity]: SemiringWithUnity[A] =
    summon[SemiringWithUnity[A]]

  given SemiringWithUnity[Int] with
    val empty: Int                   = 0
    val one: Int                     = 1
    def combine(x: Int, y: Int): Int = x + y
    def times(x: Int, y: Int): Int   = x * y
