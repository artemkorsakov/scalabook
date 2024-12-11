import cats.data.Writer

val a = Writer(Vector(
  "It was the best of times",
  "it was the worst of times"
), 1859)
val (log, result) = a.run
// val log: Vector[String] = Vector(It was the best of times, it was the worst of times)
// val result: Int = 1859