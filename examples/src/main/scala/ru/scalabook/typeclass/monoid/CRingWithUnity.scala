package ru.scalabook.typeclass.monoid

trait CRingWithUnity[A] extends CRing[A], RingWithUnity[A]

object CRingWithUnity:
  def apply[A: CRingWithUnity]: CRingWithUnity[A] =
    summon[CRingWithUnity[A]]

  given CRingWithUnity[Int] with
    val empty: Int                               = 0
    val one: Int                                 = 1
    def combine(x: Int, y: Int): Int             = x + y
    def times(x: Int, y: Int): Int               = x * y
    extension (a: Int) override def inverse: Int = -a
