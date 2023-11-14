package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.Bool.toBoolean
import ru.scalabook.books.tlpis.Dense.*
import ru.scalabook.books.tlpis.E4.{isLargerPalindrome, isPalindrome}
import weaver.FunSuite

object E4Suite extends FunSuite:
  test("Checking if a number is a palindrome"):
    expect.all(
      toBoolean[isPalindrome[_3]],
      !toBoolean[isPalindrome[_6]],
      toBoolean[isPalindrome[_15]]
    )

  test("Checking if a number is a palindrome and greater than a given number"):
    expect.all(
      toBoolean[isLargerPalindrome[_7, _3]],
      !toBoolean[isLargerPalindrome[_3, _5]],
      !toBoolean[isLargerPalindrome[_8, _5]]
    )
