def accumulate[A, B](op: (A, B) => B, initial: B, sequence: List[A]): B =
  sequence match
    case Nil          => initial
    case head :: tail => op(head, accumulate(op, initial, tail))

def accumulateN[A, B](
    op: (A, B) => B,
    initial: B,
    seqs: List[List[A]]
): List[B] =
  seqs match
    case head :: tail if head.nonEmpty =>
      accumulate(op, initial, seqs.map(_.head)) ::
        accumulateN(op, initial, seqs.map(_.tail))
    case _ => Nil

println(accumulateN[Int, Int]((x, y) => x + y, 0, List(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9), List(10,11,12))))    