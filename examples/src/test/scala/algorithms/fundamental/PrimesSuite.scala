package algorithms.fundamental

import algorithms.fundamental.Primes.*
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class PrimesSuite extends ScalaCheckSuite:
  private val theHundredthPrimeValue = 541
  private lazy val first100Primes = IndexedSeq(2, 3, 5, 7, 11, 13, 17, 19, 23,
    29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103,
    107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181,
    191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269,
    271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359,
    367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449,
    457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541)
  private val primesArray = primesNoMoreThanN(100)

  property("primes") {
    forAll(Gen.choose(0, 100)) { (k: Int) =>
      primes.take(k).toList == first100Primes.take(k)
    }
  }

  property("isPrime") {
    forAll(Gen.choose(0, theHundredthPrimeValue)) { n =>
      isPrime(n) == first100Primes.contains(n)
    }
  }

  property("isPrimeArray") {
    forAll(Gen.choose(1, theHundredthPrimeValue)) { n =>
      sieveOfEratosthenes(n).zipWithIndex.forall { case (actual, i) =>
        first100Primes.contains(i) == actual
      }
    }
  }

  property("primesNoMoreThanN") {
    forAll(Gen.choose(1, theHundredthPrimeValue)) { n =>
      assertEquals(
        primesNoMoreThanN(n).toSeq,
        first100Primes.filter(_ <= n).toSeq
      )
    }
  }

  property("primeFactorsWithPow") {
    forAll(
      Gen.oneOf(
        Seq(
          (primeFactorsWithPow(1000), Map(2 -> 3, 5 -> 3)),
          (primeFactorsWithPow(1024), Map(2 -> 10)),
          (primeFactorsWithPow(777111), Map(3 -> 1, 37 -> 1, 7001 -> 1))
        )
      )
    ) { (actual, expected) =>
      actual == expected
    }
  }

  property("nextPrime with prime array") {
    forAll(Gen.oneOf(0 until primesArray.length - 1)) { i =>
      nextPrime(primesArray(i)) == primesArray(i + 1)
    }
  }

  property("primeFactors") {
    forAll(
      Gen.oneOf(
        Seq(
          (primeFactors(1000), Set(2, 5)),
          (primeFactors(1024), Set(2)),
          (primeFactors(777111), Set(3, 37, 7001))
        )
      )
    ) { case (actual, expected) =>
      actual == expected
    }
  }
