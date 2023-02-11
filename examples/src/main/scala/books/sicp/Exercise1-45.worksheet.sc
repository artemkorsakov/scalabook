def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))

def repeated[A](f: A => A, n: Int): A => A =
  if n == 1 then f
  else repeated(compose(f, f), n - 1)

def average(a: Double, b: Double): Double = (a + b) / 2

def averageDamp(f: Double => Double): Double => Double =
  x => average(x, f(x))

val tolerance = 1e-5

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  def doesCloseEnough(a: Double, b: Double): Boolean =
    math.abs(a - b) < tolerance
  def tryGuess(guess: Double): Double =
    val next = f(guess)
    if doesCloseEnough(next, guess) then next
    else tryGuess(next)
  tryGuess(firstGuess)

def nFoldAverageFunction(f: Double => Double, n: Int): Double => Double =
  repeated(averageDamp, n)(f)

def nRoot(x: Double, n: Int): Double =
  val f: Double => Double = y => (x / math.pow(y, n - 1))
  fixedPoint(nFoldAverageFunction(f, math.log(n).toInt + 1), 1.0)

// 1
nRoot(4.0, 2)
nRoot(8.0, 3)
// 2
nRoot(16.0, 4)
nRoot(32.0, 5)
nRoot(64.0, 6)
nRoot(128.0, 7)
// 3
nRoot(256.0, 8)
nRoot(512.0, 9)
nRoot(1024.0, 10)
nRoot(math.pow(2, 11), 11)
nRoot(math.pow(2, 12), 12)
nRoot(math.pow(2, 13), 13)
nRoot(math.pow(2, 14), 14)
nRoot(math.pow(2, 15), 15)
nRoot(math.pow(2, 16), 16)
nRoot(math.pow(2, 17), 17)
nRoot(math.pow(2, 18), 18)
nRoot(math.pow(2, 19), 19)
nRoot(math.pow(2, 20), 20)

nRoot(math.pow(2, 100), 100)
