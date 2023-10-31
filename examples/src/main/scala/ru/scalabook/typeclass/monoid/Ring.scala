package ru.scalabook.typeclass.monoid

trait Ring[A] extends AbGroup[A], Semiring[A]

object Ring:
  def apply[A: Ring]: Ring[A] = summon[Ring[A]]

  given Ring[Int] with
    val empty                                    = 0
    def combine(x: Int, y: Int): Int             = x + y
    def times(x: Int, y: Int): Int               = x * y
    extension (a: Int) override def inverse: Int = -a
