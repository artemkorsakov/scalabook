package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDokuCell.SuDokuCellOperation
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SuDokuCellSuite extends SimpleIOSuite with Checkers:
  pureTest(
    "SuDokuCell.initial should create a SuDokuCell with all possible values"
  ):
    val cell = SuDokuCell.initial
    expect.all(
      cell.possibleValues == SuDokuCellValue.values.toSet,
      cell.possibleValues.size == 9,
      cell.maybeValue.isEmpty
    )

  test(
    "SuDokuCell with multiple possible values should return None as maybeValue"
  ):
    forall: (cellValues: Set[SuDokuCellValue]) =>
      val cell = SuDokuCell(cellValues)
      if cellValues.size == 1 then
        expect(cell.maybeValue.contains(cellValues.head))
      else
        expect(cell.maybeValue.isEmpty)

  pureTest("SuDokuCell execute should correctly set a single value"):
    val initialCell = SuDokuCell.initial
    val input       = (SuDokuCellOperation.Set, SuDokuCellValue.One)

    val (result, cell) = SuDokuCell.execute(input).run(initialCell)

    expect.all(
      result,
      cell.possibleValues == Set(SuDokuCellValue.One),
      cell.maybeValue.contains(SuDokuCellValue.One)
    )

  pureTest("SuDokuCell execute should correctly set a single value twice"):
    val initialCell = SuDokuCell.initial
    val input       = (SuDokuCellOperation.Set, SuDokuCellValue.One)
    val inputs      = List(input, input)

    val (result, cell) = SuDokuCell.execute(inputs*).run(initialCell)

    expect.all(
      result,
      cell.possibleValues == Set(SuDokuCellValue.One),
      cell.maybeValue.contains(SuDokuCellValue.One)
    )

  pureTest("SuDokuCell execute should correctly remove a value"):
    val initialCell = SuDokuCell(Set(SuDokuCellValue.One, SuDokuCellValue.Two))
    val input       = (SuDokuCellOperation.Remove, SuDokuCellValue.One)

    val (result, cell) = SuDokuCell.execute(input).run(initialCell)

    expect.all(
      result,
      cell.possibleValues == Set(SuDokuCellValue.Two),
      cell.maybeValue.contains(SuDokuCellValue.Two)
    )

  pureTest("SuDokuCell execute should correctly remove a value twice"):
    val initialCell = SuDokuCell(Set(SuDokuCellValue.One, SuDokuCellValue.Two))
    val input       = (SuDokuCellOperation.Remove, SuDokuCellValue.One)
    val inputs      = List(input, input)

    val (result, cell) = SuDokuCell.execute(inputs*).run(initialCell)

    expect.all(
      result,
      cell.possibleValues == Set(SuDokuCellValue.Two),
      cell.maybeValue.contains(SuDokuCellValue.Two)
    )

  pureTest(
    "SuDokuCell execute should not set a value if it is not in possibleValues"
  ):
    val initialCell = SuDokuCell(Set(SuDokuCellValue.Two))
    val input       = (SuDokuCellOperation.Set, SuDokuCellValue.One)

    val (result, cell) = SuDokuCell.execute(input).run(initialCell)

    expect.all(
      !result,
      cell == initialCell
    )

  pureTest(
    "SuDokuCell execute should not remove a value if it is the only possible value"
  ):
    val initialCell = SuDokuCell(Set(SuDokuCellValue.One))
    val input       = (SuDokuCellOperation.Remove, SuDokuCellValue.One)

    val (result, cell) = SuDokuCell.execute(input).run(initialCell)

    expect.all(
      !result,
      cell == initialCell
    )

  pureTest("SuDokuCell execute should handle multiple operations"):
    val initialCell = SuDokuCell.initial
    val inputs      = List(
      (SuDokuCellOperation.Remove, SuDokuCellValue.One),
      (SuDokuCellOperation.Set, SuDokuCellValue.Two)
    )

    val (result, cell) = SuDokuCell.execute(inputs*).run(initialCell)

    expect.all(
      result,
      cell.possibleValues == Set(SuDokuCellValue.Two),
      cell.maybeValue.contains(SuDokuCellValue.Two)
    )

  pureTest(
    "SuDokuCell execute should handle multiple operations with invalid operation"
  ):
    val initialCell = SuDokuCell.initial
    val inputs      = List(
      (SuDokuCellOperation.Remove, SuDokuCellValue.One),
      (SuDokuCellOperation.Set, SuDokuCellValue.One),
      (SuDokuCellOperation.Remove, SuDokuCellValue.Two),
      (SuDokuCellOperation.Remove, SuDokuCellValue.Three),
      (SuDokuCellOperation.Remove, SuDokuCellValue.Four)
    )

    val (result, cell) = SuDokuCell.execute(inputs*).run(initialCell)

    expect.all(
      !result,
      cell.possibleValues == Set(
        SuDokuCellValue.Two,
        SuDokuCellValue.Three,
        SuDokuCellValue.Four,
        SuDokuCellValue.Five,
        SuDokuCellValue.Six,
        SuDokuCellValue.Seven,
        SuDokuCellValue.Eight,
        SuDokuCellValue.Nine
      )
    )

end SuDokuCellSuite
