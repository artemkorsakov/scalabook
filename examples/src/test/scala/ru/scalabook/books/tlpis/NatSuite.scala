package ru.scalabook.books.tlpis

import munit.FunSuite
import ru.scalabook.books.tlpis.Bool.toBoolean
import ru.scalabook.books.tlpis.Nat.*

class NatSuite extends FunSuite:
  test("Проверка равенства Nat на 0") {
    assert(toBoolean[Is0[_0]])
    assert(!toBoolean[Is0[_2]])
  }

  test("Проверка сравнения двух Nat") {
    assert(toBoolean[(_0 Compare _0)#eq])
    assert(!toBoolean[(_0 Compare _0)#lt])
    assert(toBoolean[(_0 Compare _1)#lt])
    assert(toBoolean[(_1 Compare _0)#gt])
    assert(toBoolean[(_0 Compare _2)#lt])
    assert(toBoolean[(_2 Compare _0)#gt])
    assert(toBoolean[(_1 Compare _1)#eq])
    assert(toBoolean[(_1 Compare _2)#lt])
    assert(toBoolean[(_2 Compare _1)#gt])
    assert(toBoolean[(_3 Compare _3)#eq])
    assert(toBoolean[(_3 Compare _5)#lt])
    assert(toBoolean[(_5 Compare _3)#gt])
  }

  test("Проверка сложения двух Nat") {
    summon[Add[_0, _3] =:= _3]
    summon[Add[_3, _0] =:= _3]
    summon[Add[_3, _2] =:= _5]
  }

  test("Проверка умножения двух Nat") {
    summon[Mult[_0, _3] =:= _0]
    summon[Mult[_3, _0] =:= _0]
    summon[Mult[_3, _2] =:= _6]
  }

  test("Проверка вычисления факториала Nat") {
    summon[Fact[_3] =:= _6]
  }

  test("Проверка возведения в степень Nat") {
    summon[Exp[_3, _0] =:= _1]
    summon[Exp[_3, _1] =:= _3]
    summon[Exp[_3, _2] =:= _9]
  }

  test("Проверка вычисления модуля Nat") {
    summon[Mod[_5, _1] =:= _0]
    summon[Mod[_5, _2] =:= _1]
    summon[Mod[_5, _3] =:= _2]
    summon[Mod[_5, _4] =:= _1]
    summon[Mod[_5, _5] =:= _0]
    summon[Mod[_5, _6] =:= _5]
  }

  test("Проверка вычисления нескольких операций над Nat") {
    assert(toBoolean[Eq[Sq[_9], Add[_1, Mult[_8, _10]]]])
  }
