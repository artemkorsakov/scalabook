package ru.scalabook.algorithms.games.queens

object Queens:
  def queens(boardSize: Int): List[List[(Int, Int)]] =
    def isSafe(positions: List[(Int, Int)]): Boolean =
      positions match
        case (row, col) :: tail =>
          tail.forall: (r, c) =>
            r != row &&                              // та же строка
              math.abs(r - row) != math.abs(c - col) // та же диагональ
        case Nil => true

    def queenCols(k: Int): List[List[(Int, Int)]] =
      if k == 0 then List(List.empty)
      else
        queenCols(k - 1).flatMap: restOfQueens =>
          (1 to boardSize).map: newRow =>
            (newRow, k) :: restOfQueens
        .filter(isSafe)

    queenCols(boardSize)
  end queens
end Queens
