import scala.annotation.tailrec

def isPrime(n: Long): Boolean =
  if n < 2 then false
  else if n < 4 then true // 2 и 3 - простые
  else if n % 2 == 0 then false
  else if n < 9 then true // мы уже исключили 4,6 и 8
  else if n % 3 == 0 then false
  else
    val limit = math.ceil(math.sqrt(n.toDouble)).toLong

    @annotation.tailrec
    def loop(f: Long): Boolean =
      if f > limit then true
      else if n % f == 0 || n % (f + 2) == 0 then false
      else loop(f + 6)

    loop(5)

def nextPrime(n: Long): Option[Long] =
  if !isPrime(n) then None
  else if n == 2 then Some(3)
  else if n == 3 then Some(5)
  else
    @tailrec
    def loop(p: Long): Long =
      val nextPrime = if p % 3 == 1 then p + 4 else p + 2
      if isPrime(nextPrime) then nextPrime else loop(nextPrime)
    Some(loop(n))

def sieveOfEratosthenes(n: Int): Array[Boolean] =
  val result = Array.fill(n + 1)(true)
  result(0) = false
  result(1) = false
  (4 to n by 2).foreach(j => result(j) = false)
  for
    i <- 3 to math.sqrt(n).toInt by 2
    if result(i)
    j <- i to n / i
  do result(j * i) = false
  result

def primesNoMoreThanN(n: Int): Array[Int] =
  sieveOfEratosthenes(n).zipWithIndex.collect { case (true, prime) => prime }

def allRads(limit: Int): Array[Int] =
  val rads = new Array[Int](limit + 1)
  rads(1) = 1
  primesNoMoreThanN(limit).foreach: p =>
    (1 to limit / p).foreach: i =>
      rads(i * p) = if rads(i * p) == 0 then p else rads(i * p) * p
  rads

def isSquareFree(n: Long): Boolean =
  val s      = math.sqrt(n.toDouble).toLong

  @tailrec
  def loop(p: Long): Boolean = 
    if p > s then true
    else if n % (p * p) == 0 then false
    else 
      nextPrime(p) match
        case Some(nextP) => loop(nextP)
        case _           => true

  loop(2L)      

println(isSquareFree(10))  // true:  10 = 2 * 5
println(isSquareFree(20))  // false: 20 = 2^2 * 5
println(isSquareFree(100)) // false: 100 = 2^2 * 5^2
