package ru.scalabook.typeclass.monoid

import ru.scalabook.typeclass.monoid.Monoid.given
import ru.scalabook.typeclass.monoid.MonoidLaws.*
import weaver.*
import weaver.scalacheck.*

object MonoidSuite extends SimpleIOSuite, Checkers:
  test("Числа относительно сложения с 0"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkMonoidLaw(x, y, z)(using additionMonoid).isValid)

  test("Числа относительно умножения с 1"):
    forall: (x: Int, y: Int, z: Int) =>
      expect(checkMonoidLaw(x, y, z)(using multiplicationMonoid).isValid)

  test("Строки относительно конкатенации с пустой строкой"):
    forall: (x: String, y: String, z: String) =>
      expect(checkMonoidLaw(x, y, z)(using concatenationMonoid).isValid)

  test("Последовательность относительно операции объединения"):
    forall: (x: List[Int], y: List[Int], z: List[Int]) =>
      expect(checkMonoidLaw(x, y, z)(using listMonoid).isValid)

  test("Кортеж от двух и более моноидов"):
    forall: (x: (String, String), y: (String, String), z: (String, String)) =>
      expect(
        checkMonoidLaw(x, y, z)(using nestedMonoid[String, String]).isValid
      )

  test("`Option` является моноидом, если его параметр типа - полугруппа"):
    forall: (x: Option[String], y: Option[String], z: Option[String]) =>
      expect(checkMonoidLaw(x, y, z)(using optionMonoidInstance).isValid)

  test(
    "Множество - `Boolean`, операция - логическое И (`&&`), пустой элемент - `true`"
  ):
    forall: (x: Boolean, y: Boolean, z: Boolean) =>
      expect(checkMonoidLaw(x, y, z)(using booleanAnd).isValid)

  test(
    "Множество - `Boolean`, операция - логическое Или (`||`), пустой элемент - `false`"
  ):
    forall: (x: Boolean, y: Boolean, z: Boolean) =>
      expect(checkMonoidLaw(x, y, z)(using booleanOr).isValid)
end MonoidSuite
