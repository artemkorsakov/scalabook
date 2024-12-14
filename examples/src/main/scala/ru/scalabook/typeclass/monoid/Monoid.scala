package ru.scalabook.typeclass.monoid

trait Monoid[A] extends Semigroup[A]:
  def empty: A

object Monoid:
  def apply[A: Monoid]: Monoid[A] = summon[Monoid[A]]

  given Monoid[Int] with
    val empty                        = 0
    def combine(x: Int, y: Int): Int = x + y

  // Можно получить двойник любого моноида, просто перевернув `combine`.
  def dual[A](m: Monoid[A]): Monoid[A] = new:
    def combine(x: A, y: A): A = m.combine(y, x)
    val empty: A               = m.empty

  def endoMonoid[A]: Monoid[A => A] = new:
    def combine(f: A => A, g: A => A): A => A = f andThen g
    val empty: A => A                         = identity
