import scala.annotation.tailrec

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

def totientArray(limit: Int): Array[Long] =
  val phiArray = new Array[Long](limit + 1)
  phiArray(1) = 1

  primesNoMoreThanN(limit).foreach: p =>
    (1 to limit / p).foreach: n =>
      val k = if n % p == 0 then p else p - 1
      phiArray(n * p) = phiArray(n) * k

  phiArray

println(totientArray(20).mkString("(", ", ", ")"))
// Array(0, 1, 1, 2, 2, 4, 2, 6, 4, 6, 4, 10, 4, 12, 6, 8, 8, 16, 6, 18, 8)
//      (0, 1, 1, 2, 2, 4, 2, 6, 4, 6, 4, 10, 4, 12, 6, 8, 8, 16, 6, 18, 8)
