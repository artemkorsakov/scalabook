def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))

def repeated[A](f: A => A, n: Int): A => A =
  if n == 1 then f
  else repeated(compose(f, f), n - 1)

val square: Int => Int = x => x * x

repeated(square, 2)(5)
