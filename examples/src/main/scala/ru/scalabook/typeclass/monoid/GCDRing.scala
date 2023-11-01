package ru.scalabook.typeclass.monoid

trait GCDRing[A] extends CRingWithUnity[A]:
  def gcd(x: A, y: A): A

  def lcm(x: A, y: A): A

object GCDRing:
  def apply[A: GCDRing]: GCDRing[A] = summon[GCDRing[A]]

  given GCDRing[Int] with
    val empty: Int                   = 0
    val one: Int                     = 1
    def combine(x: Int, y: Int): Int = x + y
    def times(x: Int, y: Int): Int   = x * y
    def gcd(x: Int, y: Int): Int =
      if y == 0 then x else gcd(y, x % y)
    def lcm(x: Int, y: Int): Int = times(x, y) / gcd(x, y)

    extension (a: Int) override def inverse: Int = -a
