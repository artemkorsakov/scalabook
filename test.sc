def constructByMethod2(source: Vector[Int]): Vector[Vector[Int]] =
  (1 to source.length + 1).map { k =>
    source.map { v => if v >= k then v + 1 else v } :+ k
  }.toVector
  
println(constructByMethod2(Vector(2, 3, 1)))