package ru.scalabook.algorithms.numbers

import org.scalacheck.Gen
import ru.scalabook.algorithms.numbers.OtherNumbers.*
import spire.math.Rational
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object OtherNumbersSuite extends SimpleIOSuite with Checkers:
  private val expectedBernoulli: IndexedSeq[Rational] = IndexedSeq(
    Rational(1, 1),
    Rational(-1, 2),
    Rational(1, 6),
    Rational(0, 1),
    Rational(-1, 30),
    Rational(0, 1),
    Rational(1, 42),
    Rational(0, 1),
    Rational(-1, 30),
    Rational(0, 1),
    Rational(5, 66),
    Rational(0, 1),
    Rational(-691, 2730),
    Rational(0, 1),
    Rational(7, 6),
    Rational(0, 1),
    Rational(-3617, 510),
    Rational(0, 1),
    Rational(43867, 798),
    Rational(0, 1),
    Rational(-174611, 330),
    Rational(0, 1),
    Rational(854513, 138),
    Rational(0, 1),
    Rational(-236364091, 2730),
    Rational(0, 1),
    Rational(8553103, 6),
    Rational(0, 1),
    Rational(-23749461029L, 870),
    Rational(0, 1),
    Rational(8615841276005L, 14322),
    Rational(0, 1),
    Rational(-7709321041217L, 510),
    Rational(0, 1),
    Rational(2577687858367L, 6),
    Rational(0, 1),
    Rational(BigInt("-26315271553053477373"), BigInt(1919190)),
    Rational(0, 1),
    Rational(2929993913841559L, 6),
    Rational(0, 1),
    Rational(BigInt("-261082718496449122051"), BigInt(13530))
  )

  test("bernoulli"):
    forall(Gen.oneOf(expectedBernoulli.indices)): i =>
      expect(bernoulli(i) == expectedBernoulli(i))

  private val expectedPadovan: IndexedSeq[BigInt] = IndexedSeq(1, 0, 0, 1, 0, 1,
    1, 1, 2, 2, 3, 4, 5, 7, 9, 12, 16, 21, 28, 37, 49, 65, 86, 114, 151, 200,
    265, 351, 465, 616, 816, 1081, 1432, 1897, 2513, 3329, 4410, 5842, 7739,
    10252, 13581, 17991, 23833, 31572, 41824, 55405, 73396, 97229, 128801,
    170625)

  test("padovan"):
    forall(Gen.oneOf(expectedPadovan.indices)): i =>
      expect(padovan(i) == expectedPadovan(i))

  private val expectedJacobsthal: IndexedSeq[BigInt] = IndexedSeq(0, 1, 1, 3, 5,
    11, 21, 43, 85, 171, 341, 683, 1365, 2731, 5461, 10923, 21845, 43691, 87381,
    174763, 349525, 699051, 1398101, 2796203, 5592405, 11184811, 22369621,
    44739243, 89478485, 178956971, 357913941, 715827883, 1431655765,
    2863311531L, 5726623061L)

  test("jacobsthal"):
    forall(Gen.oneOf(expectedJacobsthal.indices)): i =>
      expect(jacobsthal(i) == expectedJacobsthal(i))

  private val expectedPell: IndexedSeq[BigInt] = IndexedSeq(0, 1, 2, 5, 12, 29,
    70, 169, 408, 985, 2378, 5741, 13860, 33461, 80782, 195025, 470832, 1136689,
    2744210, 6625109, 15994428, 38613965, 93222358, 225058681, 543339720,
    1311738121, 3166815962L, 7645370045L, 18457556052L, 44560482149L,
    107578520350L, 259717522849L)

  test("pell"):
    forall(Gen.oneOf(expectedPell.indices)): i =>
      expect(pell(i) == expectedPell(i))

  private val expectedTribonacci: IndexedSeq[BigInt] = IndexedSeq(0, 0, 1, 1, 2,
    4, 7, 13, 24, 44, 81, 149, 274, 504, 927, 1705, 3136, 5768, 10609, 19513,
    35890, 66012, 121415, 223317, 410744, 755476, 1389537, 2555757, 4700770,
    8646064, 15902591, 29249425, 53798080, 98950096, 181997601, 334745777,
    615693474, 1132436852L)

  test("tribonacci"):
    forall(Gen.oneOf(expectedTribonacci.indices)): i =>
      expect(tribonacci(i) == expectedTribonacci(i))

  private val expectedTetranacci: IndexedSeq[BigInt] = IndexedSeq(0, 0, 0, 1, 1,
    2, 4, 8, 15, 29, 56, 108, 208, 401, 773, 1490, 2872, 5536, 10671, 20569,
    39648, 76424, 147312, 283953, 547337, 1055026, 2033628, 3919944, 7555935,
    14564533, 28074040, 54114452, 104308960, 201061985, 387559437, 747044834,
    1439975216, 2775641472L)

  test("tetranacci"):
    forall(Gen.oneOf(expectedTetranacci.indices)): i =>
      expect(tetranacci(i) == expectedTetranacci(i))
end OtherNumbersSuite
