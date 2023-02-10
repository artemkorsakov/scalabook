val inc: Int => Int = _ + 1

def double[T](f: T => T): T => T = x => f(f(x))

// (((double (double double)) inc) 5)
val f: (Int => Int) => (Int => Int) = double(double(double))
f(inc)(5)
