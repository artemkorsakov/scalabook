package ru.scalabook.algorithms.str

import weaver.FunSuite

object LcsSuite extends FunSuite:
  test("lcs"):
    expect.all(
      Lcs.lcs("", "") == "",
      Lcs.lcs("abc", "") == "",
      Lcs.lcs("", "abc") == "",
      Lcs.lcs("a", "b") == "",
      Lcs.lcs("a", "a") == "a",
      Lcs.lcs("abc", "ac") == "ac",
      Lcs.lcs("abcdef", "abc") == "abc",
      Lcs.lcs("abcdef", "acf") == "acf",
      Lcs.lcs("anothertest", "notatest") == "nottest",
      Lcs.lcs("132535365", "123456789") == "12356",
      Lcs.lcs("nothardlythefinaltest", "zzzfinallyzzz") == "final",
      Lcs.lcs("abcdefghijklmnopq", "apcdefghijklmnobq") == "acdefghijklmnoq"
    )
