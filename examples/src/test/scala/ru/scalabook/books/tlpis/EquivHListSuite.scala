package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.EquivHList.*
import weaver.FunSuite

object EquivHListSuite extends FunSuite:
  final case class Example[A, B](a: A, b: Seq[B], c: Int)

  test("Checking the receipt of components by calling head/tail"):
    expect.all(
      Example('a', Seq(1, 2, 3), 19) === Example('a', Seq(1, 2, 3), 19),
      Example('b', Seq(1, 2, 3), 1) !== Example('a', Seq(1, 2, 3), 1)
    )
