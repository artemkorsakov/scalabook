package ru.scalabook.algorithms.games.sudoku

import cats.Show

enum SuDokuBlock:
  case First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth

object SuDokuBlock:
  given Show[SuDokuBlock] = Show.show[SuDokuBlock]: (block: SuDokuBlock) =>
    (block.ordinal + 1).toString

  def getBlock(row: SuDokuRow, col: SuDokuColumn): SuDokuBlock =
    (row, col) match
      case (r, c) if r.ordinal < 3 && c.ordinal < 3 => SuDokuBlock.First
      case (r, c) if r.ordinal < 3 && c.ordinal < 6 => SuDokuBlock.Second
      case (r, c) if r.ordinal < 3                  => SuDokuBlock.Third
      case (r, c) if r.ordinal < 6 && c.ordinal < 3 => SuDokuBlock.Fourth
      case (r, c) if r.ordinal < 6 && c.ordinal < 6 => SuDokuBlock.Fifth
      case (r, c) if r.ordinal < 6                  => SuDokuBlock.Sixth
      case (_, c) if c.ordinal < 3                  => SuDokuBlock.Seventh
      case (_, c) if c.ordinal < 6                  => SuDokuBlock.Eighth
      case _                                        => SuDokuBlock.Ninth
end SuDokuBlock
