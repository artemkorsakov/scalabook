val tolerance = 1e-5

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  def doesCloseEnough(a: Double, b: Double): Boolean =
    math.abs(a - b) < tolerance
  def tryGuess(guess: Double): Double =
    val next = f(guess)
    if doesCloseEnough(next, guess) then next
    else tryGuess(next)
  tryGuess(firstGuess)

val dx = 1e-6

def derivative(x: Double, g: Double => Double): Double =
  (g(x + dx) - g(x)) / dx

def newtonTransform(g: Double => Double): Double => Double =
  x => x - g(x) / derivative(x, g)

def newtonsMethod(g: Double => Double, guess: Double): Double =
  fixedPoint(newtonTransform(g), guess)

def square(x: Double): Double = x * x

def cube(x: Double): Double = x * x * x

def cubic(a: Double, b: Double, c: Double): Double => Double = x =>
  cube(x) + a * square(x) + b * x + c
