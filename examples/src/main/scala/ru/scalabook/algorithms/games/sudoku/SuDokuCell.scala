package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.fp.state.State

final case class SuDokuCell(possibleValues: Set[SuDokuCellValue]):
  lazy val maybeValue: Option[SuDokuCellValue] =
    if possibleValues.size == 1 then possibleValues.headOption
    else None

object SuDokuCell:
  val initial: SuDokuCell = SuDokuCell(SuDokuCellValue.values.toSet)

  enum SuDokuCellOper:
    case Set, Remove

  type SuDokuCellState         = State[SuDokuCell, Boolean]
  type SuDokuCellInput         = (SuDokuCellOper, SuDokuCellValue)
  opaque type SuDokuCellOutput = (Boolean, SuDokuCell)

  private val updateCell
      : SuDokuCellInput => SuDokuCellOutput => SuDokuCellOutput =
    (operation, value) =>
      (result, cell) =>
        (operation, value, result) match
          case (SuDokuCellOper.Set, value, true)
              if cell.possibleValues.contains(value) =>
            (true, SuDokuCell(Set(value)))
          case (SuDokuCellOper.Remove, value, true)
              if cell.possibleValues != Set(value) =>
            (true, SuDokuCell(cell.possibleValues - value))
          case _ =>
            (false, cell)

  def execute(inputs: List[SuDokuCellInput]): SuDokuCellState =
    State[SuDokuCell, Boolean] { cell =>
      val state =
        for
          _ <- State.traverse(inputs)(input =>
            State.modify[SuDokuCellOutput](updateCell(input))
          )
          output <- State.get
        yield output._1

      state.run((true, cell))._2
    }

end SuDokuCell
