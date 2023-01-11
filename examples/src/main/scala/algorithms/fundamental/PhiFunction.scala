package algorithms.fundamental

object PhiFunction:
  import algorithms.fundamental.Primes.primesNoMoreThanN

  def totient(n: Int): Long = totient(n.toLong)

  /** Counts the positive integers up to a given integer n that are relatively
    * prime to n.
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
        mul = mul / 2
      if number % 3 == 0 then
        while number % 3 == 0 do number /= 3
        mul = (mul / 3) * 2
      var i = 5L
      while number > 1 && i * i <= number do
        if number % i == 0 then
          while number % i == 0 do number /= i
          mul = (mul / i) * (i - 1)
        i += (if i % 6 == 5 then 2 else 4)
      if number > 1 then mul = (mul / number) * (number - 1)
      mul

  /** Euler's totient function array
    */
  @SuppressWarnings(
    Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
  )
  def totientArray(limit: Int): Array[Long] =
    val phiArray = new Array[Long](limit + 1)
    phiArray(1) = 1

    val primesArray = primesNoMoreThanN(limit)
    for (n <- 1 to limit / 2) do
      var i = 0
      while i < primesArray.length && n * primesArray(i) <= limit do
        val p = primesArray(i)
        phiArray(n * p) = phiArray(n) * (if n % p == 0 then p else p - 1)
        i += 1

    phiArray
