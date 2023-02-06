import scala.util.Random

def square(x: Long): Long = x * x

def expmod(base: Long, exp: Long, m: Long): Long =
  if exp == 0 then 1
  else if exp % 2 == 0 then
    val candidate = expmod(base, exp / 2, m)
    val root = square(candidate) % m
    if root == 1 && candidate != 1 && candidate != m - 1 then 0
    else root
  else (base * expmod(base, exp - 1, m)) % m

def fermatTest(n: Long): Boolean =
  def tryIt(a: Long): Boolean =
    expmod(a, n - 1, n) == 1
  tryIt(Random.nextLong(n - 1) + 1)

def fastIsPrime(n: Long, times: Int): Boolean =
  (times <= 0) || (fermatTest(n) && fastIsPrime(n, times - 1))

fastIsPrime(19, 100)
fastIsPrime(199, 100)
fastIsPrime(1999, 100)

fastIsPrime(561, 100)
fastIsPrime(1105, 100)
fastIsPrime(1729, 100)
fastIsPrime(2465, 100)
fastIsPrime(2821, 100)
fastIsPrime(6601, 100)
