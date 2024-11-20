package ru.scalabook.algorithms.numbers

import ru.scalabook.algorithms.fundamental.Primes.*

/** <a href="https://en.wikipedia.org/wiki/Harshad_number">Harshad number</a> */
object HarshadNumber:
  /** Число Харшада — это целое число, которое делится на сумму своих цифр.
    */
  def isHarshadNumber(number: Long): Boolean = number % sumOfDigits(number) == 0

  /** Сильное число Харшада — это число Харшада, которое при делении на сумму
    * своих цифр дает простое число.
    */
  def isStrongHarshadNumber(number: Long): Boolean =
    val sod = sumOfDigits(number)
    number % sod == 0 && isPrime(number / sod)

  /** Возвращает все сильные, усекаемые справа простые числа Харшада меньше
    * 10<sup>p</sup>.
    */
  def getStrongRightTruncatableHarshadPrimes(p: Int): Seq[Long] =
    (1 until p - 1).foldLeft((
      Seq(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L),
      Seq.empty[Long]
    )) { case ((numbers, acc), _) =>
      val next = rightTruncatableHarshadNumbers(numbers)
      val newAcc = acc ++
        next
          .withFilter(isStrongHarshadNumber)
          .flatMap(strongRightTruncatableHarshadPrimes)
      (next, newAcc)
    }._2

  /** Возьмем число 2011, которое является простым. Если отсечь из него
    * последнюю цифру, то получаем 201 - сильное число Харшада, которое также
    * отсекается вправо. Назовем такие простые числа сильными, отсекаемыми
    * справа простыми числами Харшада.
    */
  private def strongRightTruncatableHarshadPrimes(
      strongHarshadNumber: Long
  ): Seq[Long] =
    (1 to 9 by 2)
      .withFilter(i => isPrime(strongHarshadNumber * 10 + i))
      .map(strongHarshadNumber * 10 + _)

  /** 201 — это число Харшада, поскольку оно делится на 3 (сумму своих цифр).
    * Когда мы отсекаем последнюю цифру от 201, то получаем 20, что является
    * числом Харшада. Когда мы отсекаем последнюю цифру от 20, то получаем 2,
    * что также является числом Харшада. Назовем число Харшада, которое при
    * рекурсивном отсечении последней цифры всегда приводит к числу Харшада,
    * отсекаемым вправо числом Харшада.
    */
  private def rightTruncatableHarshadNumbers(
      harshadNumbers: Seq[Long]
  ): Seq[Long] =
    harshadNumbers.flatMap(rightTruncatableHarshadNumbers)

  private def rightTruncatableHarshadNumbers(harshadNumber: Long): Seq[Long] =
    val sod       = sumOfDigits(harshadNumber)
    val newNumber = harshadNumber * 10
    (0 to 9)
      .withFilter(i => (newNumber + i) % (sod + i) == 0)
      .map(newNumber + _)

  private def sumOfDigits(number: BigInt): Int =
    number.toString.foldLeft(0)((s, ch) => s + ch.asDigit)
end HarshadNumber
