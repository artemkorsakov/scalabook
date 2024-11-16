package ru.scalabook.algorithms.numbers

/** <a href="https://en.wikipedia.org/wiki/Palindrome">Palindrome</a> */
object Palindrome:
  def isPalindrome(number: Long): Boolean = isPalindrome(number, 10)

  def isPalindrome(number: Long, base: Int): Boolean =
    number == 0 || number > 0 && number % base != 0 && {
      def loop(k: Long, r: Long): Boolean =
        if k <= r then k == r || k == r / base
        else loop(k / base, r * base + k % base)

      loop(number, 0L)
    }
end Palindrome
