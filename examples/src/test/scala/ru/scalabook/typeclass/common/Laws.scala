package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import cats.syntax.either.*
import ru.scalabook.typeclass.monoid.*

object Laws:
  // Общие законы
  def checkAssociativity[A](x: A, y: A, z: A)(using
      s: Semigroup[A]
  ): ValidatedNel[String, Unit] =
    Either.cond[String, Unit](
      s.combine(s.combine(x, y), z) == s.combine(x, s.combine(y, z)),
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

  // Законы типовых классов
  def checkSemigroupLaw[A: Semigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkAssociativity(x, y, z)

  def checkMonoidLaw[A: Monoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z)
      .combine(checkIdentity(x))

  def checkGroupLaw[A: Group](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkMonoidLaw(x, y, z)
      .combine(checkInverse(x))

  def checkBandLaw[A: Band](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z)
      .combine(checkIdempotency(x))

  def checkIdempotentMonoid[A: IdempotentMonoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkMonoidLaw(x, y, z)
      .combine(checkIdempotency(x))

  def checkCSemigroup[A: CSemigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z)
      .combine(checkCommutativity(x, y))

  def checkCMonoid[A: CMonoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkMonoidLaw(x, y, z)
      .combine(checkCommutativity(x, y))

  def checkAbGroup[A: AbGroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkGroupLaw(x, y, z)
      .combine(checkCommutativity(x, y))

  def checkSemiring[A: Semiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkCMonoid(x, y, z)
      .combine(checkMultiplicativeAssociativity(x, y, z))
      .combine(checkDistributivity(x, y, z))
      .combine(checkMultiplicativePropertyOfZero(x))

  def checkSemiringWithUnity[A: SemiringWithUnity](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemiring(x, y, z)
      .combine(checkTimesIdentity(x))

  def checkCSemiring[A: CSemiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemiring(x, y, z)
      .combine(checkTimesCommutativity(x, y))

  def checkISemiring[A: ISemiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemiring(x, y, z)
      .combine(checkIdempotency(x))

  def checkRing[A: Ring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkAbGroup(x, y, z)
      .combine(checkMultiplicativeAssociativity(x, y, z))
      .combine(checkDistributivity(x, y, z))
      .combine(checkMultiplicativePropertyOfZero(x))

  def checkRingWithUnity[A: RingWithUnity](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkRing(x, y, z)
      .combine(checkTimesIdentity(x))

  def checkCRing[A: CRing](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkRing(x, y, z)
      .combine(checkTimesCommutativity(x, y))

  def checkCRingWithUnity[A: CRingWithUnity](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkCRing(x, y, z)
      .combine(checkTimesIdentity(x))

  def checkGCDRing[A: GCDRing](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkCRingWithUnity(x, y, z)
      .combine(checkGCDLCMMultiplication(x, y))
      .combine(checkGCDAssociativity(x, y, z))
      .combine(checkLCMAssociativity(x, y, z))
      .combine(checkGCDCommutativity(x, y))
      .combine(checkLCMCommutativity(x, y))

  def checkEuclideanRing[A: EuclideanRing](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkGCDRing(x, y, z)
      .combine(checkQuotModMultiplication(x, y))

  def checkField[A: Field](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkEuclideanRing(x, y, z)
      .combine(checkReciprocal(x))

end Laws
