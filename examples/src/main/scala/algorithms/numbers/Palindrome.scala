package algorithms.numbers

/** <a href="https://en.wikipedia.org/wiki/Palindrome">Palindrome</a> */
object Palindrome:
  @SuppressWarnings(
    Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
  )
  def isPalindrome(number: BigInt, base: Int): Boolean =
    number == 0 || number > 0 && number % base != 0 && {
      var revertedNumber = BigInt(0)
      var k              = number
      while k > revertedNumber
      do
        revertedNumber = revertedNumber * base + k % base
        k /= base
      k == revertedNumber || k == revertedNumber / base
    }

  def isPalindrome(number: Int): Boolean = isPalindrome(number, 10)

  @SuppressWarnings(
    Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
  )
  def isPalindrome(number: Int, base: Int): Boolean =
    number == 0 || number > 0 && number % base != 0 && {
      var revertedNumber = BigInt(0)
      var k              = number
      while k > revertedNumber
      do
        revertedNumber = revertedNumber * base + k % base
        k /= base
      k == revertedNumber || k == revertedNumber / base
    }
