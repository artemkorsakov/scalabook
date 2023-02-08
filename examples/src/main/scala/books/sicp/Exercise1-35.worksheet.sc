val tolerance = 0.00001

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  def doesCloseEnough(a: Double, b: Double): Boolean =
    math.abs(a - b) < tolerance
  def tryGuess(guess: Double): Double =
    val next = f(guess)
    if doesCloseEnough(next, guess) then next
    else tryGuess(next)
  tryGuess(firstGuess)

fixedPoint(x => 1 + (1.0 / x), 1.0)
