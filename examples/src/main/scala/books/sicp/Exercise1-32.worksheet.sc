def accumulateRec(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  if a > b then nullValue
  else
    combiner(
      term(a),
      accumulateRec(combiner, nullValue, term, next(a), next, b)
    )

def productRec(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  accumulateRec(_ * _, 1.0, term, a, next, b)

def accumulateIter(
    combiner: (Double, Double) => Double,
    nullValue: Double,
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else iter(next(a), combiner(result, term(a)))
  iter(a, nullValue)

def productIter(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  accumulateIter(_ * _, 1.0, term, a, next, b)
