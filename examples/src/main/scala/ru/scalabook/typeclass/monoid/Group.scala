package ru.scalabook.typeclass.monoid

trait Group[A] extends Monoid[A]:
  extension (a: A) def inverse: A

object Group:
  def apply[A: Group]: Group[A] = summon[Group[A]]

  given Group[Int] with
    val empty: Int                      = 0
    def combine(x: Int, y: Int): Int    = x + y
    extension (a: Int) def inverse: Int = -a
