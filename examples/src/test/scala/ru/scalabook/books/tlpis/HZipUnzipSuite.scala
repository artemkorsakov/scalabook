package ru.scalabook.books.tlpis

import munit.FunSuite
import ru.scalabook.books.tlpis.HList.*

class HZipUnzipSuite extends FunSuite:
  test("Проверка архивирования и разархивирования двух HList"):
    // разнородный список длины 3
    // с типом Int :: String :: List[Char] :: HNil
    val a = 3 :: "ai4" :: List('r', 'H') :: HNil

    // разнородный список длины 4
    // с типом Char :: Int :: Char :: String :: HNil
    val b = '3' :: 2 :: 'j' :: "sdfh" :: HNil

    // два свернутых HList-ов
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

    // разархивирование свернутых HList-ов
    val (cc1, cc2) = cc.unzip

    val (ca, cb) = cc1.unzip

    // проверка типов
    val checkCC1: (Int, Char) :: (String, Int) :: HNil.type        = cc1
    val checkCC2: (String, Int) :: (List[Char], Char) :: HNil.type = cc2
    val checkCa: Int :: String :: HNil.type                        = ca
    val checkCb: Char :: Int :: HNil.type                          = cb

    assertEquals(ca, 3 :: "ai4" :: HNil)
    assertEquals(cb, '3' :: 2 :: HNil)
