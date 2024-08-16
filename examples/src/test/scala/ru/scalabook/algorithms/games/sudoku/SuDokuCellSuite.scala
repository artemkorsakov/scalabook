package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDokuCellValue.*

import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SuDokuCellSuite extends SimpleIOSuite with Checkers:
  pureTest(
    "SuDokuCell.apply should create a SuDokuCell with all possible values"
  ):
    val cell = SuDokuCell.initial
    expect.all(
      cell.possibleValues == SuDokuCellValue.values.toSet,
      cell.possibleValues.size == 9,
      cell.maybeValue.isEmpty
    )

  test(
    "SuDokuCell with one possible value should return that value as maybeValue"
  ):
    forall: (cellValue: SuDokuCellValue) =>
      val cell = SuDokuCell(cellValue)
      whenSuccess(cell.maybeValue): actual =>
        expect.same(cellValue, actual)

  test(
    "SuDokuCell with multiple possible values should return None as maybeValue"
  ):
    forall: (cellValues: Set[SuDokuCellValue]) =>
      val cell = SuDokuCell(cellValues)
      expect(
        cellValues.size == 1 || cell.maybeValue.isEmpty
      )

  pureTest(
    "SuDokuCell with no possible values should return None as maybeValue"
  ):
    val cell = SuDokuCell(Set.empty)
    expect(cell.maybeValue.isEmpty)
end SuDokuCellSuite
