import scala.annotation.tailrec

def isHammingNumber(number: Long, n: Int): Boolean =
  if number < 2 || number <= n then true
  else if n < 2 then false
  else
    val primesList = primesNoMoreThanN(n)

    @tailrec
    def reduceLoop(n: Long, i: Long): Long =
      if n % i == 0 then reduceLoop(n / i, i) else n

    @tailrec
    def loop(n: Long, i: Int): Boolean =
      if i >= primesList.length then false
      else
        val newN = reduceLoop(n, primesList(i))
        if newN == 1 then true
        else loop(newN, i + 1)

    loop(number, 0)

private def primesNoMoreThanN(n: Int): Array[Int] =
  sieveOfEratosthenes(n).zipWithIndex.collect { case (true, prime) => prime }

private def sieveOfEratosthenes(n: Int): Array[Boolean] =
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

println(isHammingNumber(2125764000, 5))
