def cube(a: Double): Double = a * a * a

def sum(
    term: Double => Double,
    a: Double,
    next: Double => Double,
    b: Double
): Double =
  def iter(a: Double, result: Double): Double =
    if a > b then result
    else iter(next(a), result + term(a))
  iter(a, 0.0)

def integral(f: Double => Double, a: Double, b: Double, dx: Double): Double =
  def addDx(x: Double): Double = x + dx
  sum(f, a + dx / 2, addDx, b) * dx

integral(cube, 0.0, 1.0, 0.01)
integral(cube, 0.0, 1.0, 0.001)
