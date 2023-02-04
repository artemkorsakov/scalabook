def divides(a: Long, b: Long): Boolean = a % b == 0

def next(a: Long): Long =
  if a == 2 then 3 else a + 2

def findDivisor(n: Long, d: Long): Long =
  if d * d > n then n
  else if divides(n, d) then d
  else findDivisor(n, next(d))

def smallestDivisor(n: Long): Long = findDivisor(n, 2)

def isPrime(n: Long): Boolean = smallestDivisor(n) == n

Seq(1009, 1013, 1019, 10007, 10009, 10037, 100003, 100019, 100043, 1000003,
  1000033, 1000037).forall(isPrime)
