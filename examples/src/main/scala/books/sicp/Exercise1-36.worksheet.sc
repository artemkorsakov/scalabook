def average(a: Double, b: Double): Double = (a + b) / 2

val tolerance = 0.00001

def fixedPoint(f: Double => Double, firstGuess: Double): Double =
  def doesCloseEnough(a: Double, b: Double): Boolean =
    math.abs(a - b) < tolerance
  def tryGuess(guess: Double): Double =
    val next = f(guess)
    println(next)
    if doesCloseEnough(next, guess) then next
    else tryGuess(next)
  tryGuess(firstGuess)

fixedPoint(x => math.log(1000) / math.log(x), 10.0)

fixedPoint(x => average(x, math.log(1000) / math.log(x)), 10.0)
