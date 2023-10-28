package ru.scalabook.typeclass.monoid

trait AbGroup[A] extends Group[A], CMonoid[A]

object AbGroup:
  def apply[A: AbGroup]: AbGroup[A] = summon[AbGroup[A]]

  given AbGroup[Int] with
    override val empty                           = 0
    override def combine(x: Int, y: Int): Int    = x + y
    extension (a: Int) override def inverse: Int = -a
