package ru.scalabook.books.tlpis

import cats.Id
import weaver.FunSuite

object KListSuite extends FunSuite:
  final case class MyArray(str: String, str1: String)

  test("Проверка KList на примере преобразования List ~> Option"):
    val m = List(1, 2, 3, 4) :^: List("str1", "str2") :^: KNil

    val headOption: [A] => List[A] => Option[A] =
      [A] => (list: List[A]) => list.headOption

    val heads = m transform headOption
    assertEquals(
      heads,
      KCons(Some(1), KCons(Some("str1"), KNil))
    )

  test("Проверка KList на примере преобразования List ~> A"):
    val m = List(1, 2, 3, 4) :^: List("str1", "str2") :^: KNil

    val head: [A] => List[A] => Id[A] =
      [A] => (list: List[A]) => list.head

    val heads = m down head
    assertEquals(heads, 1 :: "str1" :: HNil)
