def square(x: Int): Int = x * x

def id[A]: A => A = x => x

println(id(1))
println(id(square))
println(id(id))

def min(x: Int)(y: Int): Int = if x < y then x else y
def min2(x: Int, y: Int): Int = if x < y then x else y