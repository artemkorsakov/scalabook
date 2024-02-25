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

  test("StringsOps.longestCommonSubsequence"):
    expect.all(
      longestCommonSubsequence("", "") == "",
      longestCommonSubsequence("abc", "") == "",
      longestCommonSubsequence("", "abc") == "",
      longestCommonSubsequence("a", "b") == "",
      longestCommonSubsequence("a", "a") == "a",
      longestCommonSubsequence("abc", "ac") == "ac",
      longestCommonSubsequence("abcdef", "abc") == "abc",
      longestCommonSubsequence("abcdef", "acf") == "acf",
      longestCommonSubsequence("anothertest", "notatest") == "nottest",
      longestCommonSubsequence("132535365", "123456789") == "12356",
      longestCommonSubsequence(
        "nothardlythefinaltest",
        "zzzfinallyzzz"
      ) == "final",
      longestCommonSubsequence(
        "abcdefghijklmnopq",
        "apcdefghijklmnobq"
      ) == "acdefghijklmnoq"
    )

  test("StringsOps.longestCommonSubstring"):
    expect.all(
      longestCommonSubstring("", "") == "",
      longestCommonSubstring("abc", "") == "",
      longestCommonSubstring("", "abc") == "",
      longestCommonSubstring("a", "b") == "",
      longestCommonSubstring("a", "a") == "a",
      longestCommonSubstring("abc", "ac") == "c",
      longestCommonSubstring("abcdef", "abc") == "abc",
      longestCommonSubstring("abcdef", "acf") == "f",
      longestCommonSubstring("anothertest", "notatest") == "test",
      longestCommonSubstring(
        "nothardlythefinaltest",
        "zzzfinallyzzz"
      ) == "final"
    )
end StringsOpsSuite
