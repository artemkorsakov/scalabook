package ru.scalabook.typeclass.common

import cats.data.ValidatedNel
import ru.scalabook.typeclass.monoid.*

object TypeLaws extends BasicLaws:
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
end TypeLaws
