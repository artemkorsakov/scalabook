package ru.scalabook.typeclass.monoid

trait EuclideanRing[A] extends GCDRing[A]:
  def quot(x: A, y: A): A

  def mod(x: A, y: A): A

object EuclideanRing:
  def apply[A: EuclideanRing]: EuclideanRing[A] = summon[EuclideanRing[A]]

  given EuclideanRing[Int] with
    val empty: Int                   = 0
    val one: Int                     = 1
    def combine(x: Int, y: Int): Int = x + y
    def times(x: Int, y: Int): Int   = x * y
    def gcd(x: Int, y: Int): Int =
      if y == 0 then x else gcd(y, x % y)
    def lcm(x: Int, y: Int): Int                 = times(x, y) / gcd(x, y)
    def quot(x: Int, y: Int): Int                = x / y
    def mod(x: Int, y: Int): Int                 = x % y
    extension (a: Int) override def inverse: Int = -a
