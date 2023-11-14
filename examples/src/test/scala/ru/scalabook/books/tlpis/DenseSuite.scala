package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.Dense.*
import weaver.FunSuite

object DenseSuite extends FunSuite:
  test("Check Dense's Inc"):
    summon[Inc[_0] =:= _1]
    summon[Inc[_1] =:= _2]
    summon[Inc[_2] =:= _3]
    summon[Inc[_3] =:= _4]
    summon[Inc[_4] =:= _5]
    success

  test("Check Dense's Dec"):
    summon[Dec[_0] =:= Nothing]
    summon[Dec[_1] =:= _0]
    summon[Dec[_2] =:= _1]
    summon[Dec[_3] =:= _2]
    summon[Dec[_4] =:= _3]
    summon[Dec[_5] =:= _4]
    summon[Dec[_6] =:= _5]
    summon[Dec[_7] =:= _6]
    summon[Dec[_8] =:= _7]
    summon[Dec[_9] =:= _8]
    summon[Dec[_10] =:= _9]
    success

  test("Check Dense's digit"):
    summon[_0#digit =:= Nothing]
    summon[_1#digit =:= One]
    summon[_2#digit =:= Zero]
    summon[_3#digit =:= One]
    summon[_4#digit =:= Zero]
    summon[_5#digit =:= One]
    success

  test("Check Dense's tail"):
    summon[_0#tail =:= Nothing]
    summon[_1#tail =:= _0]
    summon[_2#tail =:= _1]
    summon[_3#tail =:= _1]
    summon[_4#tail =:= _2]
    summon[_5#tail =:= _2]
    success

  test("Check Dense's ShiftR"):
    summon[_0#ShiftR =:= DNil]
    summon[_1#ShiftR =:= _0]
    summon[_2#ShiftR =:= _1]
    summon[_3#ShiftR =:= _1]
    summon[_4#ShiftR =:= _2]
    summon[_5#ShiftR =:= _2]
    success

  test("Check Dense's ShiftL"):
    summon[_0#ShiftL =:= DNil]
    summon[_1#ShiftL =:= _2]
    summon[_2#ShiftL =:= _4]
    summon[_3#ShiftL =:= _6]
    summon[_4#ShiftL =:= _8]
    success

  test("Check Dense's Add"):
    summon[(_0 Add _7) =:= _7]
    summon[(_7 Add _0) =:= _7]
    summon[(_1 Add _7) =:= _8]
    summon[(_7 Add _7) =:= _14]
    summon[(_4 Add _8) =:= _12]
    success

  test("Check Dense's Mult"):
    summon[(_0 Mult _7) =:= _0]
    summon[(_7 Mult _0) =:= _0]
    summon[(_1 Mult _7) =:= _7]
    summon[(_7 Mult _1) =:= _7]
    summon[(_4 Mult _2) =:= _8]
    success

  test("Check Dense's toInt"):
    expect.all(
      toInt[_4] == 4,
      toInt[_14] == 14,
      toInt[Inc[_5]] == 6,
      toInt[_5 Add _7] == 12,
      toInt[_12 Mult _8 Mult _13] == 1248,
      toInt[_4 Exp _15] == 1073741824
    )

  test("Check Dense's Compare"):
    summon[(_0 Compare _0) =:= EQ]
    summon[(_0 Compare _1) =:= LT]
    summon[(_1 Compare _0) =:= GT]
    summon[(_0 Compare _2) =:= LT]
    summon[(_2 Compare _0) =:= GT]
    summon[(_1 Compare _1) =:= EQ]
    summon[(_1 Compare _2) =:= LT]
    summon[(_2 Compare _1) =:= GT]
    summon[(_3 Compare _3) =:= EQ]
    summon[(_3 Compare _5) =:= LT]
    summon[(_5 Compare _3) =:= GT]
    success

  test("Check Dense's Reverse"):
    summon[Reverse[_0] =:= _0]
    summon[Reverse[_1] =:= _1]
    summon[Reverse[_2] =:= (One :: Zero :: DNil)]
    summon[Reverse[_3] =:= _3]
    summon[Reverse[_4] =:= (One :: Zero :: Zero :: DNil)]
    summon[Reverse[_5] =:= _5]
    summon[Reverse[_6] =:= (One :: One :: Zero :: DNil)]
    summon[Reverse[_7] =:= _7]
    summon[Reverse[_8] =:= (One :: Zero :: Zero :: Zero :: DNil)]
    summon[Reverse[_9] =:= _9]
    summon[Reverse[_10] =:= (One :: Zero :: One :: Zero :: DNil)]
    success
