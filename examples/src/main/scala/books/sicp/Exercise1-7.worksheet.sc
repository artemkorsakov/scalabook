def goodEnough(guess: Double, next: Double): Boolean = 
  math.abs(guess - next) < 0.001

def average(x: Double, y: Double): Double = (x + y) / 2

def improve(guess: Double, x: Double): Double =
  average(guess, x / guess)

def sqrtIter(guess: Double, x: Double): Double =
  val next = improve(guess, x)
  if goodEnough(guess, next) then guess
  else sqrtIter(next, x)

def sqrt(x: Double): Double = sqrtIter(1.0, x)

sqrt(2.0)
sqrt(1000000)  
