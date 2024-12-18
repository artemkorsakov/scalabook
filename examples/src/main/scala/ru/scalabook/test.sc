import cats.data.ValidatedNel
import cats.syntax.either.*

trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A

trait Group[A] extends Monoid[A]:
  extension (a: A)
    def inverse: A

final case class Rational(n: Int, d: Int):
  def *(that: Rational): Rational =
    Rational(n * that.n, d * that.d)

object Group:
  given Group[Int] with
    val empty: Int                      = 0
    def combine(x: Int, y: Int): Int    = x + y
    extension (a: Int) def inverse: Int = -a

  given Group[Rational] with
    val empty: Rational                             = Rational(1, 1)
    def combine(x: Rational, y: Rational): Rational = x * y
    extension (a: Rational) def inverse: Rational   = Rational(a.d, a.n)

object GroupLaws:
  def checkAssociativity[A](x: A, y: A, z: A)(using
      s: Semigroup[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      s.combine(s.combine(x, y), z) == s.combine(x, s.combine(y, z)),
      (),
      "Не соблюдается ассоциативность: (x + y) + z должно быть равно x + (y + z)"
    ).toValidatedNel

  def checkRightIdentity[A](x: A)(using
      m: Monoid[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      m.combine(x, m.empty) == x,
      (),
      "Не соблюдается тождественность справа: x + e = x"
    ).toValidatedNel

  def checkLeftIdentity[A](x: A)(using
      m: Monoid[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      m.combine(m.empty, x) == x,
      (),
      "Не соблюдается тождественность слева: e + x = x"
    ).toValidatedNel

  def checkInverse[A](x: A)(using
      g: Group[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      g.combine(x.inverse, x) == g.empty &&
        g.combine(x, x.inverse) == g.empty,
      (),
      "Не соблюдается обратимость: x + (-x) = (-x) + x = e"
    ).toValidatedNel
end GroupLaws

import Group.given
import GroupLaws.*

checkAssociativity(1, 2, 3)
// (1 + 2) + 3 = 1 + (2 + 3) = 6
checkRightIdentity(2)
checkLeftIdentity(2)
checkInverse(2)
