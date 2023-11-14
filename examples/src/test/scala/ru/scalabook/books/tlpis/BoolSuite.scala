package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.Bool.*
import weaver.FunSuite

object BoolSuite extends FunSuite:
  test("Type-level pattern matching checking for Boolean operations"):
    type Rep[A <: Bool] =
      A match
        case True  => Int
        case False => Long

    summon[Rep[True] =:= Int]
    summon[Rep[False] =:= Long]
    success

  test("Testing the logical operation && at the type level"):
    summon[(True && True) =:= True]
    summon[(True && False) =:= False]
    summon[(False && True) =:= False]
    summon[(False && False) =:= False]
    success

  test("Checking a logical operation || at the type level"):
    summon[(True || True) =:= True]
    summon[(True || False) =:= True]
    summon[(False || True) =:= True]
    summon[(False || False) =:= False]
    success

  test("Testing the logical operation Not at the type level"):
    summon[Not[True] =:= False]
    summon[Not[False] =:= True]
    success

  test("Checking the chain of logical operations at the type level"):
    summon[((True && False) || Not[False]) =:= True]
    success

  test(
    "toBoolean should convert logical operations at the type level to the value level"
  ):
    expect(toBoolean[(True && False) || Not[False]])
