package algorithms.str

import munit.FunSuite
import algorithms.str.Lcs

class LcsSuite extends FunSuite:
  test("lcs") {
    assertEquals(Lcs.lcs("", ""), "")
    assertEquals(Lcs.lcs("abc", ""), "")
    assertEquals(Lcs.lcs("", "abc"), "")
    assertEquals(Lcs.lcs("a", "b"), "")
    assertEquals(Lcs.lcs("a", "a"), "a")
    assertEquals(Lcs.lcs("abc", "ac"), "ac")
    assertEquals(Lcs.lcs("abcdef", "abc"), "abc")
    assertEquals(Lcs.lcs("abcdef", "acf"), "acf")
    assertEquals(Lcs.lcs("anothertest", "notatest"), "nottest")
    assertEquals(Lcs.lcs("132535365", "123456789"), "12356")
    assertEquals(Lcs.lcs("nothardlythefinaltest", "zzzfinallyzzz"), "final")
    assertEquals(
      Lcs.lcs("abcdefghijklmnopq", "apcdefghijklmnobq"),
      "acdefghijklmnoq"
    )
  }
