package ru.scalabook.algorithms.games.sudoku

import cats.Show

final case class SuDokuCell(possibleValues: Set[SuDokuCellValue]):
  lazy val maybeValue: Option[SuDokuCellValue] =
    if possibleValues.size == 1 then possibleValues.headOption
    else None

object SuDokuCell:
  given Show[SuDokuCell] = Show.show[SuDokuCell]: (cell: SuDokuCell) =>
    cell.possibleValues.map(v => Show[SuDokuCellValue].show(v)).toList.sorted
      .mkString("(", ",", ")")

  val initial: SuDokuCell = SuDokuCell(SuDokuCellValue.values.toSet)

  enum SuDokuCellOperation:
    case Set, Remove

  private type Input  = (SuDokuCellOperation, SuDokuCellValue)
  private type Output = (Boolean, SuDokuCell)

  private val updateCell: Input => Output => Output =
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
          case _ => // Операция не может быть выполнена
            (false, cell)

  def execute(inputs: (SuDokuCellOperation, SuDokuCellValue)*)
      : State[SuDokuCell, Boolean] =
    State[SuDokuCell, Boolean] { cell =>
      val state =
        for
          _ <- State.traverse(inputs.toList)(input =>
            State.modify[Output](updateCell(input))
          )
          (result, _) <- State.get[Output]
        yield result

      val (_, output) = state.run((true, cell))
      output
    }

end SuDokuCell
