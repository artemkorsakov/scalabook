def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))

val square: Int => Int = x => x * x
val inc: Int => Int = x => x + 1

compose(square, inc)(6)
