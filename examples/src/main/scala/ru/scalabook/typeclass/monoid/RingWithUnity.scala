package ru.scalabook.typeclass.monoid

trait RingWithUnity[A] extends Ring[A], SemiringWithUnity[A]

object RingWithUnity:
  def apply[A: RingWithUnity]: RingWithUnity[A] =
    summon[RingWithUnity[A]]

  given RingWithUnity[Int] with
    val empty: Int                               = 0
    val one: Int                                 = 1
    def combine(x: Int, y: Int): Int             = x + y
    def times(x: Int, y: Int): Int               = x * y
    extension (a: Int) override def inverse: Int = -a
