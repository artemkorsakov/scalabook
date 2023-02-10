def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))

def repeated[A](f: A => A, n: Int): A => A =
  if n == 1 then f
  else repeated(compose(f, f), n - 1)

val dx: Double = 1e-6

def smooth(f: Double => Double): Double => Double =
  x => (f(x - dx) + f(x) + f(x + dx)) / 3

def nFoldSmoothedFunction(f: Double => Double, n: Int): Double => Double =
  repeated(smooth, n)(f)
