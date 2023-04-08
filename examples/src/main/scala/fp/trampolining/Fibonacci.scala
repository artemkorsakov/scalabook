package fp.trampolining

object Fibonacci:
  def fib(n: Int): Trampoline[Int] =
    if n <= 1 then Done(n)
    else
      for
        x <- More(() => fib(n - 1))
        y <- More(() => fib(n - 2))
      yield x + y

  @main def runFib(): Unit =
    val num = fib(30).runT
    println("fibonacci = " + num)
