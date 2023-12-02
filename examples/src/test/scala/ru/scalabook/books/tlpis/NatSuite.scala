package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.Bool.toBoolean
import ru.scalabook.books.tlpis.Nat.*
import weaver.FunSuite

object NatSuite extends FunSuite:
  test("Checking Nat for 0"):
    expect.all(toBoolean[Is0[_0]], !toBoolean[Is0[_2]])

  test("Checking comparison of two Nats"):
    expect.all(
      toBoolean[(_0 Compare _0)#eq],
      !toBoolean[(_0 Compare _0)#lt],
      toBoolean[(_0 Compare _1)#lt],
      toBoolean[(_1 Compare _0)#gt],
      toBoolean[(_0 Compare _2)#lt],
      toBoolean[(_2 Compare _0)#gt],
      toBoolean[(_1 Compare _1)#eq],
      toBoolean[(_1 Compare _2)#lt],
      toBoolean[(_2 Compare _1)#gt],
      toBoolean[(_3 Compare _3)#eq],
      toBoolean[(_3 Compare _5)#lt],
      toBoolean[(_5 Compare _3)#gt]
    )

  test("Checking the addition of two Nats"):
    summon[Add[_0, _3] =:= _3]
    summon[Add[_3, _0] =:= _3]
    summon[Add[_3, _2] =:= _5]
    success

  test("Checking the multiplication of two Nats"):
    summon[Mult[_0, _3] =:= _0]
    summon[Mult[_3, _0] =:= _0]
    summon[Mult[_3, _2] =:= _6]
    success

  test("Checking the calculation of the factorial Nat"):
    summon[Fact[_3] =:= _6]
    success

  test("Checking exponentiation Nat"):
    summon[Exp[_3, _0] =:= _1]
    summon[Exp[_3, _1] =:= _3]
    summon[Exp[_3, _2] =:= _9]
    success

  test("Checking the Nat module calculation"):
    summon[Mod[_5, _1] =:= _0]
    summon[Mod[_5, _2] =:= _1]
    summon[Mod[_5, _3] =:= _2]
    summon[Mod[_5, _4] =:= _1]
    summon[Mod[_5, _5] =:= _0]
    summon[Mod[_5, _6] =:= _5]
    success

  test("Checking the calculation of several operations on Nat"):
    expect.all(toBoolean[Eq[Sq[_9], Add[_1, Mult[_8, _10]]]])
end NatSuite
