package algorithms.numbers

import munit.FunSuite
import algorithms.numbers.Palindrome.*

class PalindromeSuite extends FunSuite:
  test("isPalindrome") {
    assert(!isPalindrome(1100))
    assert(!isPalindrome(10))
    assert(isPalindrome(4994))
    assert(isPalindrome(BigInt("4668731596684224866951378664"), 10))
    assert(!isPalindrome(BigInt("4668731596684224866951378665"), 10))
  }

  test("isPalindromeByBase") {
    assert(isPalindrome(3, 2))
    assert(!isPalindrome(4, 2))
    assert(isPalindrome(15, 2))
    assert(isPalindrome(26, 3))
    assert(isPalindrome(63, 4))
    assert(!isPalindrome(4994, 2))
  }
