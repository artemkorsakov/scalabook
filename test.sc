def square(x: Int): Int = x * x
def min(a: Int, b: Int): Int = if a < b then a else b
def quad(x:Int): Int = square(square(x))
println(quad(2))
def max(a: Int, b: Int): Int = if a > b then a else b
println(max(3, 4))
def circleArea(r: Int): Double = 22.0 / 7.0 * square(r)