package ru.scalabook.books.tlpis

import munit.FunSuite
import ru.scalabook.books.tlpis.Digit.*

class DigitSuite extends FunSuite:
  test("Проверка равенства Digit на 0") {
    summon[Is0[Zero] =:= True]
    summon[Is0[One] =:= False]
  }

  test("Проверка сравнения двух Digit") {
    summon[(Zero Compare Zero) =:= EQ]
    summon[(Zero Compare One) =:= LT]
    summon[(One Compare Zero) =:= GT]
    summon[(One Compare One) =:= EQ]
  }
