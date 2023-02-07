val identity: Double => Double = x => x
val nextNum: Double => Double = x => x + 1
val square: Double => Double = x => x * x
val piFraction: Double => Double = k =>
  (2 * k) * (2 * k + 2) / square(2 * k + 1)

def productRec(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  if a > b then 1.0
  else term(a) * productRec(term, next(a), next, b)

def factorialRec(n: Int): Int =
  productRec(identity, 1, nextNum, n).toInt

def calculatePiRec(n: Int): Double =
  4 * productRec(piFraction, 1, nextNum, n)

factorialRec(10)
calculatePiRec(1000)

def productIter(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else iter(next(a), result * term(a))
  iter(a, 1.0)

def factorialIter(n: Int): Int =
  productIter(identity, 1, nextNum, n).toInt

def calculatePiIter(n: Int): Double =
  4 * productIter(piFraction, 1, nextNum, n)

factorialIter(10)
calculatePiIter(1000)
