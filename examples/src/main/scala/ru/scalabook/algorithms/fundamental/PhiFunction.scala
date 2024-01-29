package ru.scalabook.algorithms.fundamental

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
  @SuppressWarnings(
    Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
  )
  def totient(n: Long): Long =
    if n < 1 then 0
    else
      var mul    = n
      var number = n
      if number % 2 == 0 then
        while number % 2 == 0 do number /= 2
        mul /= 2
      if number % 3 == 0 then
        while number % 3 == 0 do number /= 3
        mul = (mul / 3) * 2
      var i = 5L
      while i * i <= number do
        if number % i == 0 then
          while number % i == 0 do number /= i
          mul = (mul / i) * (i - 1)
        i += (if i % 6 == 5 then 2 else 4)
      if number > 1 then mul = (mul / number) * (number - 1)
      mul

  /** Массив значений функции Эйлера. */
  @SuppressWarnings(
    Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
  )
  def totientArray(limit: Int): Array[Long] =
    val phiArray = new Array[Long](limit + 1)
    phiArray(1) = 1

    val primesArray = primesNoMoreThanN(limit)
    for n <- 1 to limit / 2 do
      var i = 0
      while i < primesArray.length && n * primesArray(i) <= limit do
        val p = primesArray(i)
        phiArray(n * p) = phiArray(n) * (if n % p == 0 then p else p - 1)
        i += 1

    phiArray
end PhiFunction
