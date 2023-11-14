package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.Digit.*
import weaver.FunSuite

object DigitSuite extends FunSuite:
  test("Testing Digit for 0"):
    summon[Is0[Zero] =:= True]
    summon[Is0[One] =:= False]
    success

  test("Checking comparison of two Digits"):
    summon[(Zero Compare Zero) =:= EQ]
    summon[(Zero Compare One) =:= LT]
    summon[(One Compare Zero) =:= GT]
    summon[(One Compare One) =:= EQ]
    success
