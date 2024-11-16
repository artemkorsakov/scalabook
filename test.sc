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

println(nextPrime(503)) // Some(509)