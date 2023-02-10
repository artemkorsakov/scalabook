def contFracIter(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int, result: Double): Double =
    if i == 0 then result
    else loop(i - 1, n(i) / (d(i) + result))
  loop(k, 0.0)

def n(x: Double)(i: Int): Double =
  if i == 1 then x
  else -x * x

val d: Int => Double = i => 2 * (i - 1) + 1

def tanCf(x: Double, k: Int): Double =
  contFracIter(n(x), d, k)

tanCf(1.0, 100)
