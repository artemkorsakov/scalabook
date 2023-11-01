package ru.scalabook.typeclass.monoid

trait CRing[A] extends Ring[A]

object CRing:
  def apply[A: CRing]: CRing[A] = summon[CRing[A]]

  given CRing[Int] with
    val empty                                    = 0
    def combine(x: Int, y: Int): Int             = x + y
    def times(x: Int, y: Int): Int               = x * y
    extension (a: Int) override def inverse: Int = -a
