package algorithms.fundamental

import algorithms.fundamental.Primes.*
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class PrimesSuite extends ScalaCheckSuite:
  private val theHundredthPrimeValue = 541
  private lazy val first100Primes = IndexedSeq(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67,
    71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191,
    193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317,
    331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461,
    463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541)

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
    forAll(
      Gen.oneOf(
        Seq(
          (
            isPrimeArray(20),
            Array(false, false, true, true, false, true, false, true, false, false, false, true, false, true, false,
              false, false, true, false, true, false)
          )
        )
      )
    ) { case (actual, expected) =>
      actual sameElements expected
    }
  }
