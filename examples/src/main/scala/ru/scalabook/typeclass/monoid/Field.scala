package ru.scalabook.typeclass.monoid

import spire.math.Rational

trait Field[A] extends EuclideanRing[A]:
  extension (a: A) def reciprocal: A

  def div(x: A, y: A): A = times(x, y.reciprocal)

object Field:
  def apply[A: Field]: Field[A] = summon[Field[A]]

  given Field[Rational] with
    val empty: Rational = Rational(0)
    val one: Rational   = Rational(1)

    def combine(x: Rational, y: Rational): Rational = x + y

    def times(x: Rational, y: Rational): Rational = x * y

    def gcd(x: Rational, y: Rational): Rational = one

    def lcm(x: Rational, y: Rational): Rational = times(x, y)

    def quot(x: Rational, y: Rational): Rational = x / y

    def mod(x: Rational, y: Rational): Rational = empty

    extension (a: Rational)
      def inverse: Rational    = Rational(-a.numerator, a.denominator)
      def reciprocal: Rational = Rational(a.denominator, a.numerator)
  end given
end Field
