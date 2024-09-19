def accumulate[A, B](op: (A, B) => B, initial: B, sequence: List[A]): B =
  sequence match
    case Nil          => initial
    case head :: tail => op(head, accumulate(op, initial, tail))

def map[A, B](f: A => B, sequence: List[A]): List[B] =
  accumulate[A, List[B]]((a, listB) => f(a) :: listB, List.empty[B], sequence)

def append[A](seq1: List[A], seq2: List[A]): List[A] =
  accumulate[A, List[A]]((a, listA) => a :: listA, seq1, seq2)

def flatMap[A, B](proc: A => List[B], sequence: List[A]): List[B] =
  accumulate(
    (a: A, listB: List[B]) => append(proc(a), listB),
    Nil: List[B],
    sequence
  )

def filter[A](p: A => Boolean, list: List[A]): List[A] =
  flatMap(
    (a: A) => if p(a) then List(a) else Nil,
    list
  )

def queens(boardSize: Int): List[List[(Int, Int)]] =
  val emptyBoard: List[(Int, Int)] = List.empty

  def adjoinPosition(
      newRow: Int,
      k: Int,
      restOfQueens: List[(Int, Int)]
  ): List[(Int, Int)] = (newRow, k) :: restOfQueens

  def isSafe(positions: List[(Int, Int)]): Boolean =
    positions match
      case Nil => true
      case head :: tail =>
        val (row, col) = head
        tail.forall(_._1 != row) && // same row
        tail.forall(_._2 != col) && // same column
        tail.forall: (r, c) =>
          math.abs(r - row) != math.abs(c - col) // same diagonal

  def queenCols(k: Int): List[List[(Int, Int)]] =
    if k == 0 then List(emptyBoard)
    else
      filter(
        (positions: List[(Int, Int)]) => isSafe(positions),
        flatMap(
          (restOfQueens: List[(Int, Int)]) =>
            map(
              (newRow: Int) => adjoinPosition(newRow, k, restOfQueens),
              (1 to boardSize).toList
            ),
          queenCols(k - 1)
        )
      )

  queenCols(boardSize)
end queens

println(queens(8).head)
println(queens(8))
