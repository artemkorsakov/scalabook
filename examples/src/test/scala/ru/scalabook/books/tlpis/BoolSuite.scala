package ru.scalabook.books.tlpis

import munit.FunSuite
import ru.scalabook.books.tlpis.Bool.*

class BoolSuite extends FunSuite:
  test(
    "Проверка сопоставления с шаблоном на уровне типов для логических операций"
  ) {
    type Rep[A <: Bool] =
      A match
        case True  => Int
        case False => Long

    summon[Rep[True] =:= Int]
    summon[Rep[False] =:= Long]
  }

  test("Проверка логической операции && на уровне типов") {
    summon[(True && True) =:= True]
    summon[(True && False) =:= False]
    summon[(False && True) =:= False]
    summon[(False && False) =:= False]
  }

  test("Проверка логической операции || на уровне типов") {
    summon[(True || True) =:= True]
    summon[(True || False) =:= True]
    summon[(False || True) =:= True]
    summon[(False || False) =:= False]
  }

  test("Проверка логической операции Not на уровне типов") {
    summon[Not[True] =:= False]
    summon[Not[False] =:= True]
  }

  test("Проверка цепочки логических операций на уровне типов") {
    summon[((True && False) || Not[False]) =:= True]
  }

  test(
    "toBoolean должен логические операции на уровне типов преобразовывать на уровень значений"
  ) {
    assert(toBoolean[(True && False) || Not[False]])
  }
