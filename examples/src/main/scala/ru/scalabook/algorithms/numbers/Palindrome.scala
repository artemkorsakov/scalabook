package ru.scalabook.algorithms.numbers

/** <a href="https://en.wikipedia.org/wiki/Palindrome">Palindrome</a> */
object Palindrome:
  def isPalindrome(number: Long): Boolean = isPalindrome(number, 10)

  @SuppressWarnings(
    Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
  )
  def isPalindrome(number: Long, base: Int): Boolean =
    number == 0 || number > 0 && number % base != 0 && {
      var revertedNumber = 0L
      var k              = number
      while k > revertedNumber
      do
        revertedNumber = revertedNumber * base + k % base
        k /= base
      k == revertedNumber || k == revertedNumber / base
    }
end Palindrome
