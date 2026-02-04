package ru.scalabook.algorithms.fundamental

import org.scalacheck.Gen
import ru.scalabook.algorithms.fundamental.Primes.*
import weaver.*
import weaver.scalacheck.*

object PrimesSuite extends SimpleIOSuite with Checkers:
  private val theHundredthPrimeValue = 541
  private lazy val first100Primes    = IndexedSeq(
    2, 3, 5, 7, 11, 13, 17, 19, 23,
    29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103,
    107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181,
    191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269,
    271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359,
    367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449,
    457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541
  )
  private val primesArray = primesNoMoreThanN(100)

  test("primes"):
    forall(Gen.choose(0, 100)): (k: Int) =>
      expect.all(primes.take(k).toList == first100Primes.take(k))

  test("isPrime"):
    forall(Gen.choose(0, theHundredthPrimeValue)): n =>
      expect.all(isPrime(n) == first100Primes.contains(n))

  test("isPrimeArray"):
    forall(Gen.choose(1, theHundredthPrimeValue)): n =>
      expect.all(sieveOfEratosthenes(n).zipWithIndex.forall {
        case (actual, i) =>
          first100Primes.contains(i) == actual
      })

  test("primesNoMoreThanN"):
    forall(Gen.choose(1, theHundredthPrimeValue)): n =>
      expect.all(
        primesNoMoreThanN(n).toSeq == first100Primes.filter(_ <= n)
      )

  test("primeFactorsWithPow"):
    forall(
      Gen.oneOf(
        Seq(
          (primeFactorsWithPow(1000), Map(2 -> 3, 5 -> 3)),
          (primeFactorsWithPow(1024), Map(2 -> 10)),
          (primeFactorsWithPow(777111), Map(3 -> 1, 37 -> 1, 7001 -> 1))
        )
      )
    ): (actual, expected) =>
      expect.all(actual == expected)

  test("primeFactors"):
    forall(
      Gen.oneOf(
        Seq(
          (primeFactors(1000), List(5, 2)),
          (primeFactors(1024), List(2)),
          (primeFactors(777111), List(7001, 37, 3))
        )
      )
    ): (actual, expected) =>
      expect.all(actual == expected)

  test("nextPrime with prime array"):
    forall(Gen.oneOf(0 until primesArray.length - 1)): i =>
      matches(nextPrime(primesArray(i))) { case Some(actual) =>
        expect.all(primesArray(i + 1) == actual)
      }
end PrimesSuite
