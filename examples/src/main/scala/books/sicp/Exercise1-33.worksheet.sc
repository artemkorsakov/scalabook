def filteredAccumulateRec(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double,
    filter: Double => Boolean
): Double =
  if a > b then nullValue
  else
    val nextA = term(a)
    lazy val res =
      filteredAccumulateRec(combiner, nullValue, term, next(a), next, b, filter)
    if filter(nextA) then combiner(nextA, res)
    else res

def filteredAccumulateIter(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double,
    filter: Double => Boolean
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else
      val nextA = term(a)
      val nextResult =
        if filter(nextA) then combiner(result, nextA)
        else result
      iter(next(a), nextResult)
  iter(a, nullValue)

def filteredAccumulate(
    combiner: (Int, Int) => Int,
    nullValue: Int,
    term: Int => Int,
    a: Int,
    next: Int => Int,
    b: Int,
    filter: Int => Boolean
): Int = ???

def isPrime(n: Int): Boolean = ???

def sumPrimeSquares(a: Int, b: Int): Int =
  filteredAccumulate(
    combiner = (x, y) => x + y,
    nullValue = 0,
    term = x => x * x,
    a = a,
    next = x => x + 1,
    b = b,
    filter = isPrime
  )

def gcd(a: Int, b: Int): Int = ???

def productPrimesLessThanN(n: Int): Int =
  filteredAccumulate(
    combiner = (x, y) => x * y,
    nullValue = 1,
    term = x => x,
    a = 1,
    next = x => x + 1,
    b = n - 1,
    filter = i => gcd(i, n) == 1
  )
