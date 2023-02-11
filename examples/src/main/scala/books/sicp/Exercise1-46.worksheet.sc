def iterativeImprove(
    doesCloseEnough: (Double, Double) => Boolean,
    next: Double => Double
): Double => Double = firstGuess =>
  def tryGuess(guess: Double): Double =
    val nextGuess = next(guess)
    if doesCloseEnough(nextGuess, guess) then nextGuess
    else tryGuess(nextGuess)
  tryGuess(firstGuess)

def doesCloseEnough(a: Double, b: Double): Boolean =
  math.abs(a - b) < 1e-5

def average(x: Double, y: Double): Double = (x + y) / 2

def sqrt(x: Double): Double =
  iterativeImprove(doesCloseEnough, y => average(y, x / y))(1.0)

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  iterativeImprove(doesCloseEnough, f)(firstGuess)

sqrt(4.0)

def averageDamp(f: Double => Double): Double => Double =
  x => average(x, f(x))

def cubeRoot(x: Double): Double =
  fixedPoint(averageDamp(y => x / (y * y)), 1.0)

cubeRoot(8.0)
