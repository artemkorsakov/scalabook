def square(x: Double): Double = x * x

def goodEnough(guess: Double, next: Double): Boolean = 
  math.abs(guess - next) < 0.001

def improve(guess: Double, x: Double): Double =
  ((x / square(guess)) + 2 * guess) / 3

def cubeIter(guess: Double, x: Double): Double =
  val next = improve(guess, x)
  if goodEnough(guess, next) then next
  else cubeIter(next, x)

def cubeOf(x: Double): Double = cubeIter(1.0, x)

cubeOf(2.0)
cubeOf(1000000)  
