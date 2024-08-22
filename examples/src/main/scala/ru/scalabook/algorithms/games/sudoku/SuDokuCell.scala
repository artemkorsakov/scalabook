package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.fp.state.State

final case class SuDokuCell(possibleValues: Set[SuDokuCellValue]):
  lazy val maybeValue: Option[SuDokuCellValue] =
    if possibleValues.size == 1 then possibleValues.headOption
    else None

object SuDokuCell:
  val initial: SuDokuCell = SuDokuCell(SuDokuCellValue.values.toSet)

  enum SuDokuCellOperation:
    case Set, Remove

  type SuDokuCellInput         = (SuDokuCellOperation, SuDokuCellValue)
  opaque type SuDokuCellOutput = (Boolean, SuDokuCell)

  private val updateCell
      : SuDokuCellInput => SuDokuCellOutput => SuDokuCellOutput =
    (operation, value) =>
      (result, cell) =>
        (operation, value, result) match
          // Устанавливаем значение в ячейке только если это значение среди допустимых
          case (SuDokuCellOperation.Set, value, true)
              if cell.possibleValues.contains(value) =>
            (true, SuDokuCell(Set(value)))
          // Удаляем значение в ячейке только если это не единственное значение из допустимых
          case (SuDokuCellOperation.Remove, value, true)
              if cell.possibleValues != Set(value) =>
            (true, SuDokuCell(cell.possibleValues - value))
          case _ =>
            (false, cell)

  def execute(inputs: SuDokuCellInput*): State[SuDokuCell, Boolean] =
    State[SuDokuCell, Boolean] { cell =>
      val state =
        for
          _ <- State.traverse(inputs.toList)(input =>
            State.modify[SuDokuCellOutput](updateCell(input))
          )
          output <- State.get
        yield output._1

      state.run((true, cell))._2
    }

end SuDokuCell
