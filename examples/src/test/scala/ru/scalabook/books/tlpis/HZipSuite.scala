package ru.scalabook.books.tlpis

import munit.FunSuite
import ru.scalabook.books.tlpis.HList.*

class HZipSuite extends FunSuite:
  test("Проверка свертки двух HList"):
    // разнородный список длины 3
    // с типом Int :: String :: List[Char] :: HNil
    val a = 3 :: "ai4" :: List('r', 'H') :: HNil

    // разнородный список длины 4
    // с типом Char :: Int :: Char :: String :: HNil
    val b = '3' :: 2 :: 'j' :: "sdfh" :: HNil

    // два свернутых HLists
    val c = a zip b
    assertEquals(
      c,
      (3, '3') :: ("ai4", 2) :: (List('r', 'H'), 'j') :: HNil
    )

    // ещё одна свертка
    val cc = c zip c.tail
    assertEquals(
      cc,
      ((3, '3'), ("ai4", 2)) :: (("ai4", 2), (List('r', 'H'), 'j')) :: HNil
    )

    // проверка типов
    // заметим, что четвертый элемент b удален,
    // также как это происходит при свертке однородных списков List
    val checkCType
        : (Int, Char) :: (String, Int) :: (List[Char], Char) :: HNil.type = c

    val checkCCType: ((Int, Char), (String, Int)) :: (
        (String, Int),
        (List[Char], Char)
    ) :: HNil.type = cc
