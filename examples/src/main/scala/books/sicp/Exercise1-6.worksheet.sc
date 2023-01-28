def square(x: Double): Double = x * x

def goodEnough(guess: Double, x: Double): Boolean = 
  math.abs(square(guess) - x) < 0.001

def improve(guess: Double, x: Double): Double =
  average(guess, x / guess)

def average(x: Double, y: Double): Double = (x + y) / 2

def sqrtIter(guess: Double, x: Double): Double =
  if goodEnough(guess, x) then guess
  else sqrtIter(improve(guess, x), x)

def sqrt(x: Double): Double = sqrtIter(1.0, x)

sqrt(2.0)  
