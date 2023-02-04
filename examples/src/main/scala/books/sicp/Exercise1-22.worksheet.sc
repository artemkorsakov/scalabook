import java.time.Instant

def divides(a: Long, b: Long): Boolean = a % b == 0

def findDivisor(n: Long, d: Long): Long =
  if d * d > n then n
  else if divides(n, d) then d
  else findDivisor(n, d + 1)

def smallestDivisor(n: Long): Long = findDivisor(n, 2)

def isPrime(n: Long): Boolean = smallestDivisor(n) == n

def reportPrime(elapsedTime: Long): Unit =
  println(" *** ")
  println(s"${elapsedTime} ms")

def startPrimeTest(n: Long, startTime: Long): Option[Long] =
  if isPrime(n) then
    println(s"n = $n")
    reportPrime(Instant.now().toEpochMilli - startTime)
    Some(n)
  else None

def timedPrimeTest(n: Long): Option[Long] =
  startPrimeTest(n, Instant.now().toEpochMilli())

timedPrimeTest(1999)

def searchForPrimes(start: Long, end: Long): Seq[Long] =
  (start to end).flatMap(timedPrimeTest)

searchForPrimes(1000, 1000 + 1000)
searchForPrimes(10000, 10000 + 1000)
searchForPrimes(100000, 100000 + 1000)
searchForPrimes(1000000, 1000000 + 1000)
