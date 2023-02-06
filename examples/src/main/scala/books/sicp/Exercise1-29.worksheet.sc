def cube(a: Double): Double = a * a * a

def sum(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  if a > b then 0
  else term(a) + sum(term, next(a), next, b)

def integral(f: Double => Double, a: Double, b: Double, dx: Double): Double =
  def addDx(x: Double): Double = x + dx
  sum(f, a + dx / 2, addDx, b) * dx

integral(cube, 0.0, 1.0, 0.01)
integral(cube, 0.0, 1.0, 0.001)

def simpsonRule(f: Double => Double, a: Double, b: Double, n: Int): Double =
  val h = (b - a) / n
  def y(k: Int): Double =
    val co = if k == 0 || k == n then 1 else if k % 2 == 0 then 2 else 4
    co * f(a + k * h)

  (0 to n).foldLeft(0.0)((acc, k) => acc + y(k)) * h / 3

simpsonRule(cube, 0.0, 1.0, 100)
simpsonRule(cube, 0.0, 1.0, 1000)
