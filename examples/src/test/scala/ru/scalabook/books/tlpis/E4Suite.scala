package ru.scalabook.books.tlpis

import munit.FunSuite
import ru.scalabook.books.tlpis.Bool.toBoolean
import ru.scalabook.books.tlpis.Dense.*
import ru.scalabook.books.tlpis.E4.{isLargerPalindrome, isPalindrome}

class E4Suite extends FunSuite:
  test("Проверка, является ли число палиндромом") {
    assert(toBoolean[isPalindrome[_3]])
    assert(!toBoolean[isPalindrome[_6]])
    assert(toBoolean[isPalindrome[_15]])
  }

  test("Проверка, является ли число палиндромом и больше ли заданного") {
    assert(toBoolean[isLargerPalindrome[_7, _3]])
    assert(!toBoolean[isLargerPalindrome[_3, _5]])
    assert(!toBoolean[isLargerPalindrome[_8, _5]])
  }
