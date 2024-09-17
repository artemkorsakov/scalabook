def foldRight[A, B](op: (A, B) => B, initial: B, sequence: List[A]): B =
  sequence match
    case Nil          => initial
    case head :: tail => op(head, foldRight(op, initial, tail))

def foldLeft[A, B](op: (B, A) => B, initial: B, sequence: List[A]): B =
  def iter(result: B, rest: List[A]): B =
    rest match
      case Nil => result
      case head :: next =>
        iter(op(result, head), next)

  iter(initial, sequence)

println(foldRight[Double, Double](_ / _, 1.0, List(1.0, 2.0, 3.0)))
println(foldLeft[Double, Double](_ / _, 1.0, List(1.0, 2.0, 3.0)))

println(foldRight[Int, List[Int]](_ :: _, Nil, List(1, 2, 3)))
println(foldLeft[Int, List[Int]]((b, a) => a :: b, Nil, List(1, 2, 3)))

def reverseViaFoldRight[A](sequence: List[A]): List[A] =
  foldRight((a: A, b: List[A]) => b :+ a, Nil, sequence)

def reverseViaFoldLeft[A](sequence: List[A]): List[A] =
  foldLeft((b: List[A], a: A) => a :: b, Nil, sequence)

println(reverseViaFoldRight(List(1, 2, 3)))
println(reverseViaFoldLeft(List(1, 2, 3)))
