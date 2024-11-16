package ru.scalabook.algorithms.fundamental

import scala.annotation.tailrec

object PhiFunction:
  import ru.scalabook.algorithms.fundamental.Primes.primesNoMoreThanN

  /** Количество натуральных чисел, меньших либо равных n и взаимно простых с
    * ним.
    *
    * @see
    *   <a
    *   href="https://en.wikipedia.org/wiki/Euler%27s_totient_function">detailed
    *   description</a>
    */
  def totient(n: Long): Long =
    if n < 1 then 0
    else
      @tailrec
      def reduceLoop(n: Long, i: Long): Long =
        if n % i == 0 then reduceLoop(n / i, i) else n

      @tailrec
      def loop(n: Long, i: Long, acc: Long): Long =
        val limit = math.sqrt(n.toDouble)
        if i > limit then
          if n > 1 then acc * (n - 1) / n else acc
        else if n % i == 0 then
          loop(reduceLoop(n, i), i + 2, acc * (i - 1) / i)
        else loop(n, i + 2, acc)

      if n % 2 == 0 then loop(reduceLoop(n, 2), 3, n / 2)
      else loop(n, 3, n)

  /** Массив значений функции Эйлера. */
  def totientArray(limit: Int): Array[Long] =
    val phiArray = new Array[Long](limit + 1)
    phiArray(1) = 1

    primesNoMoreThanN(limit).foreach: p =>
      (1 to limit / p).foreach: n =>
        val k = if n % p == 0 then p else p - 1
        phiArray(n * p) = phiArray(n) * k

    phiArray
end PhiFunction
