package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.Palindrome.*
import weaver.FunSuite

object PalindromeSuite extends FunSuite:
  test("isPalindrome"):
    expect.all(
      !isPalindrome(1100),
      !isPalindrome(10),
      isPalindrome(4994),
      isPalindrome(BigInt("4668731596684224866951378664"), 10),
      !isPalindrome(BigInt("4668731596684224866951378665"), 10)
    )

  test("isPalindromeByBase"):
    expect.all(
      isPalindrome(3, 2),
      !isPalindrome(4, 2),
      isPalindrome(15, 2),
      isPalindrome(26, 3),
      isPalindrome(63, 4),
      !isPalindrome(4994, 2)
    )
