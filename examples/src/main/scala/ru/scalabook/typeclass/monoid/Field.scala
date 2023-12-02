package ru.scalabook.typeclass.monoid

import ru.scalabook.algorithms.numbers.RationalNumber

trait Field[A] extends EuclideanRing[A]:
  extension (a: A) def reciprocal: A

  def div(x: A, y: A): A = times(x, y.reciprocal)

object Field:
  def apply[A: Field]: Field[A] = summon[Field[A]]

  given Field[RationalNumber] with
    val empty: RationalNumber = RationalNumber.ZeroRationalNumber
    val one: RationalNumber   = RationalNumber.OneRationalNumber

    def combine(x: RationalNumber, y: RationalNumber): RationalNumber = x + y

    def times(x: RationalNumber, y: RationalNumber): RationalNumber = x * y

    def gcd(x: RationalNumber, y: RationalNumber): RationalNumber = one

    def lcm(x: RationalNumber, y: RationalNumber): RationalNumber = times(x, y)

    def quot(x: RationalNumber, y: RationalNumber): RationalNumber = x / y

    def mod(x: RationalNumber, y: RationalNumber): RationalNumber = empty

    extension (a: RationalNumber)
      def inverse: RationalNumber    = RationalNumber(-a.x, a.y)
      def reciprocal: RationalNumber = RationalNumber(a.y, a.x)
  end given
end Field
