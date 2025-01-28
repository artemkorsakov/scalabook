package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.monad.{Plus, PlusEmpty}
import ru.scalabook.typeclass.monoid.*

trait BasicLaws:
  def check(condition: Boolean, message: String): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](condition, (), message).toValidatedNel

  def checkAssociativity[A](x: A, y: A, z: A)(using
      s: Semigroup[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      s.combine(s.combine(x, y), z) == s.combine(x, s.combine(y, z)),
      (),
      "Не соблюдается ассоциативность: (x + y) + z должно быть равно x + (y + z)"
    ).toValidatedNel

  def checkPlusAssociativity[F[_], A](f1: F[A], f2: F[A], f3: F[A])(using
      pl: Plus[F]
  ): ValidatedNel[String, Unit] =
    import pl.*
    Either.cond[String, Unit](
      plus(plus(f1, f2), f3) == plus(f1, plus(f2, f3)),
      (),
      "Не соблюдается ассоциативность: (x + y) + z должно быть равно x + (y + z)"
    ).toValidatedNel

  def checkMultiplicativeAssociativity[A](x: A, y: A, z: A)(using
      ms: MultiplicativeSemigroup[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      ms.times(ms.times(x, y), z) == ms.times(x, ms.times(y, z)),
      (),
      "Не соблюдается ассоциативность по умножению: (x * y) * z должно быть равно x * (y * z)"
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

  def checkIdentity[A: Monoid](x: A): ValidatedNel[String, Unit] =
    checkRightIdentity(x).combine(checkLeftIdentity(x))

  def checkPlusEmptyRightIdentity[F[_], A](fa: F[A])(using
      pl: PlusEmpty[F]
  ): ValidatedNel[String, Unit] =
    import pl.*
    Either.cond[String, Unit](
      plus(fa, empty) == fa,
      (),
      "Не соблюдается тождественность справа: x + e = x"
    ).toValidatedNel

  def checkPlusEmptyLeftIdentity[F[_], A](fa: F[A])(using
      pl: PlusEmpty[F]
  ): ValidatedNel[String, Unit] =
    import pl.*
    Either.cond[String, Unit](
      plus(empty, fa) == fa,
      (),
      "Не соблюдается тождественность слева: e + x = x"
    ).toValidatedNel

  def checkPlusEmptyIdentity[F[_]: PlusEmpty, A](fa: F[A])
      : ValidatedNel[String, Unit] =
    checkPlusEmptyRightIdentity(fa).combine(checkPlusEmptyLeftIdentity(fa))

  def checkRightTimesIdentity[A](x: A)(using
      su: SemiringWithUnity[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      su.times(x, su.one) == x,
      (),
      "Не соблюдается существование единичного элемента справа: x * 1 = x"
    ).toValidatedNel

  def checkLeftTimesIdentity[A](x: A)(using
      su: SemiringWithUnity[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      su.times(su.one, x) == x,
      (),
      "Не соблюдается существование единичного элемента слева: 1 * x = x"
    ).toValidatedNel

  def checkTimesIdentity[A: SemiringWithUnity](x: A)
      : ValidatedNel[String, Unit] =
    checkRightTimesIdentity(x).combine(checkLeftTimesIdentity(x))

  def checkInverse[A](x: A)(using
      g: Group[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      g.combine(x.inverse, x) == g.empty &&
        g.combine(x, x.inverse) == g.empty,
      (),
      "Не соблюдается обратимость: x + (-x) = (-x) + x = e"
    ).toValidatedNel

  def checkIdempotency[A](x: A)(using s: Band[A]): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      s.combine(x, x) == x,
      (),
      "Не соблюдается идемпотентность: x + x должно быть равно x"
    ).toValidatedNel

  def checkCommutativity[A](x: A, y: A)(using
      cs: CSemigroup[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      cs.combine(x, y) == cs.combine(y, x),
      (),
      "Не соблюдается коммутативность: x + y должно быть равно y + x"
    ).toValidatedNel

  def checkTimesCommutativity[A](x: A, y: A)(using
      cs: CSemiring[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      cs.times(x, y) == cs.times(y, x),
      (),
      "Не соблюдается коммутативность по умножению: x * y = y * x"
    ).toValidatedNel

  def checkRightDistributivity[A](x: A, y: A, z: A)(using
      sr: Semiring[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      sr.times(sr.combine(x, y), z) == sr.combine(
        sr.times(x, z),
        sr.times(y, z)
      ),
      (),
      "Не соблюдается дистрибутивность справа: (x + y) * z = x * z + y * z"
    ).toValidatedNel

  def checkLeftDistributivity[A](x: A, y: A, z: A)(using
      sr: Semiring[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      sr.times(x, sr.combine(y, z)) == sr.combine(
        sr.times(x, y),
        sr.times(x, z)
      ),
      (),
      "Не соблюдается дистрибутивность слева: x * (y + z) = x * y + x * z"
    ).toValidatedNel

  def checkDistributivity[A: Semiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkRightDistributivity(x, y, z)
      .combine(checkLeftDistributivity(x, y, z))

  def checkRightMultiplicativePropertyOfZero[A](x: A)(using
      sr: Semiring[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      sr.times(sr.empty, x) == sr.empty,
      (),
      "Не соблюдается мультипликативное свойство нуля справа: 0 * a = 0"
    ).toValidatedNel

  def checkLeftMultiplicativePropertyOfZero[A](x: A)(using
      sr: Semiring[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      sr.times(x, sr.empty) == sr.empty,
      (),
      "Не соблюдается мультипликативное свойство нуля слева: a * 0 = 0"
    ).toValidatedNel

  def checkMultiplicativePropertyOfZero[A: Semiring](x: A)
      : ValidatedNel[String, Unit] =
    checkRightMultiplicativePropertyOfZero(x)
      .combine(checkLeftMultiplicativePropertyOfZero(x))

  def checkGCDLCMMultiplication[A](x: A, y: A)(using
      gr: GCDRing[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      gr.times(gr.gcd(x, y), gr.lcm(x, y)) == gr.times(y, x),
      (),
      "Произведение НОД и НОК должно равняться произведению чисел: gcd(x, y) * lcm(x, y) = x * y"
    ).toValidatedNel

  def checkGCDAssociativity[A](x: A, y: A, z: A)(using
      gr: GCDRing[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      gr.gcd(gr.gcd(x, y), z) == gr.gcd(x, gr.gcd(y, z)),
      (),
      "Не соблюдается ассоциативность НОД: gcd(gcd(x, y), z) = gcd(x, gcd(y, z))"
    ).toValidatedNel

  def checkLCMAssociativity[A](x: A, y: A, z: A)(using
      gr: GCDRing[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      gr.lcm(gr.lcm(x, y), z) == gr.lcm(x, gr.lcm(y, z)),
      (),
      "Не соблюдается ассоциативность НОК: lcm(lcm(x, y), z) = lcm(x, lcm(y, z))"
    ).toValidatedNel

  def checkGCDCommutativity[A](x: A, y: A)(using
      gr: GCDRing[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      gr.gcd(x, y) == gr.gcd(y, x),
      (),
      "Не соблюдается коммутативность НОД: gcd(x, y) = gcd(y, x)"
    ).toValidatedNel

  def checkLCMCommutativity[A](x: A, y: A)(using
      gr: GCDRing[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      gr.lcm(x, y) == gr.lcm(y, x),
      (),
      "Не соблюдается коммутативность НОК: lcm(x, y) = lcm(y, x)"
    ).toValidatedNel

  def checkQuotModMultiplication[A](x: A, y: A)(using
      er: EuclideanRing[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      er.combine(er.times(y, er.quot(x, y)), er.mod(x, y)) == x,
      (),
      "Для частного и остатка должен соблюдаться закон: b * (a /~ b) + (a % b) = a"
    ).toValidatedNel

  def checkReciprocal[A](x: A)(using fi: Field[A]): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      x == fi.empty || fi.times(x, x.reciprocal) == fi.one,
      (),
      "Не соблюдается закон существования обратного по умножению числа: x * x^{-1} = 1"
    ).toValidatedNel
end BasicLaws
