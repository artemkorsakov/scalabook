package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.common.MonoidLaws.*
import ru.scalabook.typeclass.monoid.Semigroup.given
import weaver.*
import weaver.scalacheck.*

object SemigroupSuite extends SimpleIOSuite, Checkers:
  test("Натуральные числа образуют полугруппу по сложению"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkSemigroupLaw(x, y, z)(using additionSemigroup).isValid)

  test("Натуральные числа N образуют полугруппу относительно умножения"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkSemigroupLaw(x, y, z)(using multiplicationSemigroup).isValid)

  test("Строки образуют полугруппу относительно конкатенации"):
    forall: (x: String, y: String, z: String) =>
      expect(checkSemigroupLaw(x, y, z).isValid)

  test(
    "Последовательность образует полугруппу относительно операции объединения"
  ):
    forall: (x: List[Int], y: List[Int], z: List[Int]) =>
      expect(checkSemigroupLaw(x, y, z).isValid)

  pureTest("Натуральные числа не образуют полугруппу по вычитанию"):
    given Semigroup[Int] = (x, y) => x - y
    expect(checkSemigroupLaw(1, 2, 3).isInvalid)
end SemigroupSuite
