package algorithms.numbers

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import algorithms.numbers.OtherNumbers.*

class OtherNumbersSuite extends ScalaCheckSuite:
  private val expectedNumerators: IndexedSeq[BigDecimal]   = IndexedSeq(
    1,
    -1,
    1,
    0,
    -1,
    0,
    1,
    0,
    -1,
    0,
    5,
    0,
    -691,
    0,
    7,
    0,
    -3617,
    0,
    43867,
    0,
    -174611,
    0,
    854513,
    0,
    -236364091,
    0,
    8553103,
    0,
    -23749461029L,
    0,
    8615841276005L,
    0,
    -7709321041217L,
    0,
    2577687858367L,
    0,
    BigDecimal("-26315271553053477373"),
    0,
    2929993913841559L,
    0,
    BigDecimal("-261082718496449122051")
  )
  private val expectedDenominators: IndexedSeq[BigDecimal] =
    IndexedSeq(1, 2, 6, 1, 30, 1, 42, 1, 30, 1, 66, 1, 2730, 1, 6, 1, 510, 1,
      798, 1, 330, 1, 138, 1, 2730, 1, 6, 1, 870, 1, 14322, 1, 510, 1, 6, 1,
      1919190, 1, 6, 1, 13530)

  property("bernoulli") {
    forAll(Gen.oneOf(expectedNumerators.indices)) { i =>
      math.abs(
        (bernoulli(i) - expectedNumerators(i) / expectedDenominators(
          i
        )).toDouble
      ) <= 1e-5
    }
  }

  private val expectedPadovan: IndexedSeq[BigInt] = IndexedSeq(1, 0, 0, 1, 0, 1,
    1, 1, 2, 2, 3, 4, 5, 7, 9, 12, 16, 21, 28, 37, 49, 65, 86, 114, 151, 200,
    265, 351, 465, 616, 816, 1081, 1432, 1897, 2513, 3329, 4410, 5842, 7739,
    10252, 13581, 17991, 23833, 31572, 41824, 55405, 73396, 97229, 128801,
    170625)

  property("padovan") {
    forAll(Gen.oneOf(expectedPadovan.indices)) { i =>
      padovan(i) == expectedPadovan(i)
    }
  }

  private val expectedJacobsthal: IndexedSeq[BigInt] = IndexedSeq(0, 1, 1, 3, 5,
    11, 21, 43, 85, 171, 341, 683, 1365, 2731, 5461, 10923, 21845, 43691, 87381,
    174763, 349525, 699051, 1398101, 2796203, 5592405, 11184811, 22369621,
    44739243, 89478485, 178956971, 357913941, 715827883, 1431655765,
    2863311531L, 5726623061L)

  property("jacobsthal") {
    forAll(Gen.oneOf(expectedJacobsthal.indices)) { i =>
      jacobsthal(i) == expectedJacobsthal(i)
    }
  }

  private val expectedPell: IndexedSeq[BigInt] = IndexedSeq(0, 1, 2, 5, 12, 29,
    70, 169, 408, 985, 2378, 5741, 13860, 33461, 80782, 195025, 470832, 1136689,
    2744210, 6625109, 15994428, 38613965, 93222358, 225058681, 543339720,
    1311738121, 3166815962L, 7645370045L, 18457556052L, 44560482149L,
    107578520350L, 259717522849L)

  property("pell") {
    forAll(Gen.oneOf(expectedPell.indices)) { i =>
      pell(i) == expectedPell(i)
    }
  }

  private val expectedTribonacci: IndexedSeq[BigInt] = IndexedSeq(0, 0, 1, 1, 2,
    4, 7, 13, 24, 44, 81, 149, 274, 504, 927, 1705, 3136, 5768, 10609, 19513,
    35890, 66012, 121415, 223317, 410744, 755476, 1389537, 2555757, 4700770,
    8646064, 15902591, 29249425, 53798080, 98950096, 181997601, 334745777,
    615693474, 1132436852L)

  property("tribonacci") {
    forAll(Gen.oneOf(expectedTribonacci.indices)) { i =>
      tribonacci(i) == expectedTribonacci(i)
    }
  }

  private val expectedTetranacci: IndexedSeq[BigInt] = IndexedSeq(0, 0, 0, 1, 1,
    2, 4, 8, 15, 29, 56, 108, 208, 401, 773, 1490, 2872, 5536, 10671, 20569,
    39648, 76424, 147312, 283953, 547337, 1055026, 2033628, 3919944, 7555935,
    14564533, 28074040, 54114452, 104308960, 201061985, 387559437, 747044834,
    1439975216, 2775641472L)

  property("tetranacci") {
    forAll(Gen.oneOf(expectedTetranacci.indices)) { i =>
      tetranacci(i) == expectedTetranacci(i)
    }
  }
