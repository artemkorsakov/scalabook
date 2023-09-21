package ru.scalabook.books.tlpis

import munit.FunSuite
import ru.scalabook.books.tlpis.EquivHList.*

class EquivHListSuite extends FunSuite:
  final case class Example[A, B](a: A, b: Seq[B], c: Int)

  test("Проверка получения компонентов по вызову head/tail"):
    assert(Example('a', Seq(1, 2, 3), 19) === Example('a', Seq(1, 2, 3), 19))
    assert(Example('b', Seq(1, 2, 3), 1) !== Example('a', Seq(1, 2, 3), 1))
