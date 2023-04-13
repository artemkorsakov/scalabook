package fp.trampolining

object EvenOdd:
  def even[A](ns: List[A]): Trampoline[Boolean] =
    ns match
      case Nil     => Done(true)
      case _ :: xs => More(() => odd(xs))

  def odd[A](ns: List[A]): Trampoline[Boolean] =
    ns match
      case Nil     => Done(false)
      case _ :: xs => More(() => even(xs))

  @main def runEvenList(): Unit =
    val list = List.fill(100000)(0)
    println(s"list is even = ${even(list).runT}")
