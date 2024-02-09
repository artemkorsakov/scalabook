package ru.scalabook.algorithms.str

import ru.scalabook.algorithms.str.StringsOps.*
import weaver.FunSuite

object StringsOpsSuite extends FunSuite:
  test("StringsOps.isPalindrome"):
    expect.all(
      isPalindrome("лёша на полке клопа нашёл".replaceAll("\\s", "")),
      !isPalindrome("Лёша на полке клопа нашёл")
    )

  test("StringsOps.longestPalindrome"):
    expect.all(longestPalindrome("BBABBCAB") == "BBABB")

  test("StringsOps.lcs"):
    expect.all(
      lcs("", "") == "",
      lcs("abc", "") == "",
      lcs("", "abc") == "",
      lcs("a", "b") == "",
      lcs("a", "a") == "a",
      lcs("abc", "ac") == "ac",
      lcs("abcdef", "abc") == "abc",
      lcs("abcdef", "acf") == "acf",
      lcs("anothertest", "notatest") == "nottest",
      lcs("132535365", "123456789") == "12356",
      lcs("nothardlythefinaltest", "zzzfinallyzzz") == "final",
      lcs("abcdefghijklmnopq", "apcdefghijklmnobq") == "acdefghijklmnoq"
    )
end StringsOpsSuite
