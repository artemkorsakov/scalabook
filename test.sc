def isPalindrome(number: Long): Boolean = isPalindrome(number, 10)

// Является ли заданное число палиндромом в системе исчисления по заданному состоянию
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

// Примеры использования
val numbersToTest = List(121, 12321, 10, 11, 123, 1001, 10001)
// Проверка на палиндром в десятичной системе
println("Проверка на палиндром в десятичной системе:")
numbersToTest.foreach: number =>
  println(s"$number: ${isPalindrome(number)}")

// Проверка на палиндром в двоичной системе
println("\nПроверка на палиндром в двоичной системе:")
numbersToTest.foreach: number =>
  println(s"$number: ${isPalindrome(number, 2)}")

// Проверка на палиндром в шестнадцатеричной системе
println("\nПроверка на палиндром в шестнадцатеричной системе:")
numbersToTest.foreach: number =>
  println(s"$number: ${isPalindrome(number, 16)}")
