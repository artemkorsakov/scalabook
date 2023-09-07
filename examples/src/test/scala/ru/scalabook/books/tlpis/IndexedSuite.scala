package ru.scalabook.books.tlpis

import munit.FunSuite
import ru.scalabook.books.tlpis.Indexed.given
import ru.scalabook.books.tlpis.Nat.*

class IndexedSuite extends FunSuite:
  private val x = 3 :: true :: "asfd" :: false :: 'k' :: () :: 13 :: 9.3 :: HNil

  test("получение булева значения по индексу 3"):
    val b2: Boolean = x.i[_3].at
    assert(!b2)

  test(
    "удалить все значения перед индексом 3 и затем получить первое значение"
  ):
    val pre: Boolean = x.i[_3].drop.i[_0].at
    assert(!pre)

  test("замена булева значения по индексу 3 на число 19"):
    val rep = x.i[_3].replace(19)
    assertEquals(
      rep,
      3 :: true :: "asfd" :: 19 :: 'k' :: () :: 13 :: 9.3 :: HNil
    )

  test(
    "преобразование значения по индексу 4 на `true` если символ в нижнем регистре, иначе - `false`"
  ):
    val mp = x.i[_4].map(_.isLower)
    assertEquals(
      mp,
      3 :: true :: "asfd" :: false :: true :: () :: 13 :: 9.3 :: HNil
    )

  test("Удаление значения по индексу 5"):
    val rm = x.i[_5].remove
    assertEquals(
      rm,
      3 :: true :: "asfd" :: false :: 'k' :: 13 :: 9.3 :: HNil
    )
