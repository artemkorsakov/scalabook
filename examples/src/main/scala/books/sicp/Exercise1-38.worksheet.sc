def contFracIter(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int, result: Double): Double =
    if i == 0 then result
    else loop(i - 1, n(i) / (d(i) + result))
  loop(k, 0.0)

val n: Int => Double = _ => 1.0

val d: Int => Double = i =>
  if i % 3 == 2 then (i / 3 + 1) * 2
  else 1.0

contFracIter(n, d, 1000)
