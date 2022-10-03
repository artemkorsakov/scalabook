package algorithms.fundamental

object Primes:
  lazy val primes: LazyList[Int] =
    2 #:: LazyList
      .from(3)
      .filter { x =>
        val sqrtOfPrimes = primes.takeWhile(p => p <= math.sqrt(x))
        sqrtOfPrimes.forall(p => x % p != 0)
      }

  def isPrime(n: Long): Boolean =
    if n < 2 then false
    else if n < 4 then true // 2 и 3 - простые
    else if n % 2 == 0 then false
    else if n < 9 then true // мы уже исключили 4,6 и 8
    else if n % 3 == 0 then false
    else
      val limit = math.ceil(math.sqrt(n)).toLong

      @annotation.tailrec
      def loop(f: Long): Boolean =
        if f > limit then true
        else if n % f == 0 || n % (f + 2) == 0 then false
        else loop(f + 6)

      loop(5)

  def isPrimeArray(n: Int): Array[Boolean] =
    val result = Array.fill(n + 1)(true)
    result(0) = false
    result(1) = false
    var i = 2
    while i * i <= n do
      if result(i) then (i * i to n by i).foreach(j => result(j) = false)
      i += 1
    result
