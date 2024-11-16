import scala.annotation.tailrec

def primeFactorsWithPow(n: Long): Map[Long, Int] =
  @tailrec
  def reduceLoop(n: Long, i: Long, count: Int): (Long, Int) =
    if n % i == 0 then reduceLoop(n / i, i, count + 1) else (n, count)

  @tailrec
  def loop(n: Long, i: Long, acc: Map[Long, Int]): Map[Long, Int] =
    val limit = math.sqrt(n.toDouble)
    if i > limit then
      if n > 1 then acc + (n -> 1) else acc
    else if n % i == 0 then
      val (newN, newCount) = reduceLoop(n, i, 0)
      loop(newN, i + 2, acc + (i -> newCount))
    else loop(n, i + 2, acc)

  if n % 2 == 0 then
    val (newN, newCount) = reduceLoop(n, 2, 0)
    loop(newN, 3, Map(2L -> newCount))
  else loop(n, 3, Map.empty)
end primeFactorsWithPow

def sumOfDivisors(number: Long): Long =
  val primeDivisors = primeFactorsWithPow(number)
  primeDivisors.foldLeft(1L) { case (mul, (prime, power)) =>
    val num = math.pow(prime, power + 1).toLong - 1
    val den = prime - 1
    mul * (num / den)
  }

println(sumOfDivisors(84))

def countOfDivisors(number: Long): Long =
  primeFactorsWithPow(number).values.foldLeft(1L): (mul, a) =>
    mul * (a + 1)

println(countOfDivisors(84))
