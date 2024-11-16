val A = 7
val B = 5
val M = 11
val generator = LazyList.iterate(0)(x => (A * x + B) % M)

println(generator.take(11).toList)
// List(0, 5, 7, 10, 9, 2, 8, 6, 3, 4, 0)
// List(0, 5, 7, 10, 9, 2, 8, 6, 3, 4, 0)