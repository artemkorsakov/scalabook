package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.numbers.Palindrome.isPalindrome

@main def isPalindromeBench(): Unit =
  println((1 to 1000000).count(isPalindrome))
