def square(x: Int): Int = x * x

def sumOfSquares(x: Int, y: Int): Int = square(x) + square(y)

def f(a: Int, b: Int, c: Int): Int =
  if a <= b && a <= c then sumOfSquares(b, c)
  else if b <= c then sumOfSquares(a, c)
  else sumOfSquares(a, b)

f(5, 3, 4)  
