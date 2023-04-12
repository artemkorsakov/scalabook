case class Container[A](value: A):
  def flatMap[B](f: A => Container[B]): Container[B] = f(value)
  def map[B](f: A => B): Container[B]                = Container(f(value))

val f: Int => Container[Boolean] = i => Container(i % 2 == 0)
val g: Boolean => String         = b => if b then "четное" else "нечетное"

for
  a <- Container(7)
  b <- f(a)
yield g(b)

// Эквивалентно: Container(7).flatMap(f).map(g)