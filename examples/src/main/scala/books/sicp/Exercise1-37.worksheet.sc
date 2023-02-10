def contFracRec(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int): Double =
    if i == k then n(i) / d(i)
    else n(i) / (d(i) + loop(i + 1))
  loop(1)

contFracRec(_ => 1.0, _ => 1.0, 11)

def contFracIter(n: Int => Double, d: Int => Double, k: Int): Double =
  def loop(i: Int, result: Double): Double =
    if i == 0 then result
    else loop(i - 1, n(i) / (d(i) + result))
  loop(k, 0.0)

contFracIter(_ => 1.0, _ => 1.0, 11)
