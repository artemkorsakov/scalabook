package ru.scalabook.algorithms.games.sudoku

import cats.syntax.show.*
import ru.scalabook.algorithms.games.sudoku.SuDokuCellValue.*
import ru.scalabook.algorithms.games.sudoku.SuDokuSolver.*
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SuDokuSolverSuite extends SimpleIOSuite with Checkers:
  private val initialValues = SuDokuCellValue.values.toSet

  pureTest("SuDoku.setValue should correctly set a value in a cell - example"):
    val initialSudoku = SuDoku.initial
    val (result, sudoku) =
      SuDokuSolver.setValue(
        SuDokuRow.First,
        SuDokuColumn.Third,
        SuDokuCellValue.Three
      ).run(initialSudoku)

    expect.all(
      result,
      sudoku.show ==
        "|(1,2,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(3)                |(1,2,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |\n" +
        "|(1,2,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|\n" +
        "|(1,2,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|\n" +
        "|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,4,5,6,7,8,9)  |(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|\n" +
        "|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,4,5,6,7,8,9)  |(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|\n" +
        "|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,4,5,6,7,8,9)  |(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|\n" +
        "|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,4,5,6,7,8,9)  |(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|\n" +
        "|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,4,5,6,7,8,9)  |(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|\n" +
        "|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,4,5,6,7,8,9)  |(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|(1,2,3,4,5,6,7,8,9)|"
    )

  test("SuDoku.setValue should correctly set a value in a cell"):
    forall: (row: SuDokuRow, col: SuDokuColumn, value: SuDokuCellValue) =>
      val initialSudoku    = SuDoku.initial
      val state            = SuDokuSolver.setValue(row, col, value)
      val (result, sudoku) = state.run(initialSudoku)

      val expectedValues = SuDokuCellValue.values.toSet - value
      val block          = SuDokuBlock.getBlock(row, col)

      expect(result).and(
        forEach(sudoku.cells.toVector):
          (index: SuDokuIndex, cell: SuDokuCell) =>
            if index._1 == row && index._2 == col then
              expect(cell.maybeValue.contains(value))
            else if index._1 == row && index._2 != col ||
              index._1 != row && index._2 == col ||
              index._3 == block && (index._1 != row || index._2 != col)
            then
              expect(cell.possibleValues == initialValues - value)
            else
              expect(cell.possibleValues == initialValues)
      )

  pureTest("SuDoku.setValue should false if one of the operators fails"):
    val initialSudoku = SuDoku.initial
    val inputs: Seq[SuDokuInput] = Seq(
      (SuDokuRow.First, SuDokuColumn.First, SuDokuCellValue.One),
      (SuDokuRow.First, SuDokuColumn.Second, SuDokuCellValue.Two),
      (SuDokuRow.First, SuDokuColumn.Third, SuDokuCellValue.Three),
      (SuDokuRow.First, SuDokuColumn.Fourth, SuDokuCellValue.Four),
      (SuDokuRow.First, SuDokuColumn.Fifth, SuDokuCellValue.Five),
      (SuDokuRow.First, SuDokuColumn.Sixth, SuDokuCellValue.Six),
      (SuDokuRow.First, SuDokuColumn.Seventh, SuDokuCellValue.Seven),
      (SuDokuRow.First, SuDokuColumn.Eighth, SuDokuCellValue.Eight),
      (SuDokuRow.Ninth, SuDokuColumn.Ninth, SuDokuCellValue.Nine)
    )
    val (result, sudoku) =
      SuDokuSolver.setValues(inputs*).run(initialSudoku)

    val expectedSudoku =
      "|(1)                |(2)                |(3)                |(4)                |(5)                |(6)                |(7)                |(8)                |(9)                |\n" +
        "|(4,5,6,7,8,9)      |(4,5,6,7,8,9)      |(4,5,6,7,8,9)      |(1,2,3,7,8,9)      |(1,2,3,7,8,9)      |(1,2,3,7,8,9)      |(1,2,3,4,5,6,9)    |(1,2,3,4,5,6,9)    |(1,2,3,4,5,6,9)    |\n" +
        "|(4,5,6,7,8,9)      |(4,5,6,7,8,9)      |(4,5,6,7,8,9)      |(1,2,3,7,8,9)      |(1,2,3,7,8,9)      |(1,2,3,7,8,9)      |(1,2,3,4,5,6,9)    |(1,2,3,4,5,6,9)    |(1,2,3,4,5,6,9)    |\n" +
        "|(2,3,4,5,6,7,8,9)  |(1,3,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,3,5,6,7,8,9)  |(1,2,3,4,6,7,8,9)  |(1,2,3,4,5,7,8,9)  |(1,2,3,4,5,6,8,9)  |(1,2,3,4,5,6,7,9)  |(1,2,3,4,5,6,7,8,9)|\n" +
        "|(2,3,4,5,6,7,8,9)  |(1,3,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,3,5,6,7,8,9)  |(1,2,3,4,6,7,8,9)  |(1,2,3,4,5,7,8,9)  |(1,2,3,4,5,6,8,9)  |(1,2,3,4,5,6,7,9)  |(1,2,3,4,5,6,7,8,9)|\n" +
        "|(2,3,4,5,6,7,8,9)  |(1,3,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,3,5,6,7,8,9)  |(1,2,3,4,6,7,8,9)  |(1,2,3,4,5,7,8,9)  |(1,2,3,4,5,6,8,9)  |(1,2,3,4,5,6,7,9)  |(1,2,3,4,5,6,7,8,9)|\n" +
        "|(2,3,4,5,6,7,8,9)  |(1,3,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,3,5,6,7,8,9)  |(1,2,3,4,6,7,8,9)  |(1,2,3,4,5,7,8,9)  |(1,2,3,4,5,6,8,9)  |(1,2,3,4,5,6,7,9)  |(1,2,3,4,5,6,7,8,9)|\n" +
        "|(2,3,4,5,6,7,8,9)  |(1,3,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,3,5,6,7,8,9)  |(1,2,3,4,6,7,8,9)  |(1,2,3,4,5,7,8,9)  |(1,2,3,4,5,6,8,9)  |(1,2,3,4,5,6,7,9)  |(1,2,3,4,5,6,7,8,9)|\n" +
        "|(2,3,4,5,6,7,8,9)  |(1,3,4,5,6,7,8,9)  |(1,2,4,5,6,7,8,9)  |(1,2,3,5,6,7,8,9)  |(1,2,3,4,6,7,8,9)  |(1,2,3,4,5,7,8,9)  |(1,2,3,4,5,6,8,9)  |(1,2,3,4,5,6,7,9)  |(1,2,3,4,5,6,7,8,9)|"

    expect.all(
      !result,
      sudoku.show == expectedSudoku
    )

  pureTest(
    "SuDoku.hasSolution should return true if a SuDoku puzzle has a solution"
  ):
    val (convertedResult, convertedSudoku) = SuDokuSolver.toSuDoku(
      "483921657\n" +
        "967345821\n" +
        "251876493\n" +
        "548132976\n" +
        "729564138\n" +
        "136798245\n" +
        "372689514\n" +
        "814253769\n" +
        "695417382"
    )

    expect.all(
      convertedResult,
      convertedSudoku.isCorrect,
      convertedSudoku.hasSolution
    )

  pureTest("SuDoku.crossOutLoop should return initial sudoku if no next row"):
    val initialSudoku = SuDoku.initial

    val (result, sudoku) =
      crossOutLoop(
        initialSudoku,
        maybeRow = None,
        maybeColumn = Some(SuDokuColumn.First)
      )

    expect.all(
      result,
      sudoku == initialSudoku
    )

  pureTest(
    "SuDoku.crossOutLoop should return initial sudoku if no next column"
  ):
    val initialSudoku = SuDoku.initial

    val (result, sudoku) =
      crossOutLoop(
        initialSudoku,
        maybeRow = Some(SuDokuRow.First),
        maybeColumn = None
      )

    expect.all(
      result,
      sudoku == initialSudoku
    )

  pureTest("SuDoku.crossOutLoop should return initial sudoku if no cell"):
    val initialSudoku = SuDoku(Map.empty)

    val (result, sudoku) =
      crossOutLoop(
        initialSudoku,
        maybeRow = Some(SuDokuRow.First),
        maybeColumn = Some(SuDokuColumn.First)
      )

    expect.all(
      !result,
      sudoku == initialSudoku
    )

  pureTest(
    "SuDoku.crossOutLoop should return initial sudoku if there is a solution"
  ):
    val (initialResult, initialSudoku) = SuDokuSolver.toSuDoku(
      "483921657\n" +
        "967345821\n" +
        "251876493\n" +
        "548132976\n" +
        "729564138\n" +
        "136798245\n" +
        "372689514\n" +
        "814253769\n" +
        "695417382"
    )

    val (result, sudoku) =
      crossOutLoop(
        initialSudoku,
        maybeRow = Some(SuDokuRow.First),
        maybeColumn = Some(SuDokuColumn.First)
      )

    expect.all(
      initialResult,
      result,
      sudoku == initialSudoku
    )

  pureTest("SuDoku.crossOutLoop should cross out an invalid value"):
    val (initialResult, initialSudoku) = SuDokuSolver.toSuDoku(
      "483921657\n" +
        "967345821\n" +
        "251876493\n" +
        "548132976\n" +
        "729564138\n" +
        "136798245\n" +
        "372689514\n" +
        "814253769\n" +
        "695417382"
    )

    val index = (SuDokuRow.Ninth, SuDokuColumn.Seventh, SuDokuBlock.Ninth)
    val updatedSudoku = SuDoku(initialSudoku.cells.updated(
      index,
      SuDokuCell(Set(
        SuDokuCellValue.Three,
        SuDokuCellValue.Six,
        SuDokuCellValue.Five
      ))
    ))

    val (result, sudoku) =
      crossOutLoop(
        updatedSudoku,
        maybeRow = Some(SuDokuRow.Ninth),
        maybeColumn = Some(SuDokuColumn.Seventh)
      )

    expect.all(
      initialResult,
      result,
      sudoku == initialSudoku
    )

  pureTest(
    "SuDoku.crossOutLoop should cross out an invalid value for the last cell"
  ):
    val (initialResult, initialSudoku) = SuDokuSolver.toSuDoku(
      "483921657\n" +
        "967345821\n" +
        "251876493\n" +
        "548132976\n" +
        "729564138\n" +
        "136798245\n" +
        "372689514\n" +
        "814253769\n" +
        "695417382"
    )

    val index = (SuDokuRow.Ninth, SuDokuColumn.Ninth, SuDokuBlock.Ninth)
    val updatedSudoku = SuDoku(initialSudoku.cells.updated(
      index,
      SuDokuCell(Set(
        SuDokuCellValue.Two,
        SuDokuCellValue.Six,
        SuDokuCellValue.Five
      ))
    ))

    val (result, sudoku) =
      crossOutLoop(
        updatedSudoku,
        maybeRow = Some(SuDokuRow.Ninth),
        maybeColumn = Some(SuDokuColumn.Seventh)
      )

    expect.all(
      initialResult,
      result,
      sudoku == initialSudoku
    )

  pureTest(
    "SuDoku.getSingleValues should return a list of the single values in a SuDoku puzzle"
  ):
    val (result, sudoku) = SuDokuSolver.toSuDoku(
      "000000907\n" +
        "000420180\n" +
        "300705426\n" +
        "100904000\n" +
        "050000040\n" +
        "000507009\n" +
        "926108000\n" +
        "834059000\n" +
        "517000000"
    )

    val singleValuesFirstBlock   = getSingleValues(sudoku, SuDokuBlock.First)
    val singleValuesSecondBlock  = getSingleValues(sudoku, SuDokuBlock.Second)
    val singleValuesThirdBlock   = getSingleValues(sudoku, SuDokuBlock.Third)
    val singleValuesFourthBlock  = getSingleValues(sudoku, SuDokuBlock.Fourth)
    val singleValuesFifthBlock   = getSingleValues(sudoku, SuDokuBlock.Fifth)
    val singleValuesSixthBlock   = getSingleValues(sudoku, SuDokuBlock.Sixth)
    val singleValuesSeventhBlock = getSingleValues(sudoku, SuDokuBlock.Seventh)
    val singleValuesEighthBlock  = getSingleValues(sudoku, SuDokuBlock.Eighth)
    val singleValuesNinthBlock   = getSingleValues(sudoku, SuDokuBlock.Ninth)

    expect.all(
      result,
      singleValuesFirstBlock.isEmpty,
      singleValuesSecondBlock == List((
        SuDokuRow.Third,
        SuDokuColumn.Fifth,
        SuDokuCellValue.Nine
      )),
      singleValuesThirdBlock.isEmpty,
      singleValuesFourthBlock == List((
        SuDokuRow.Fifth,
        SuDokuColumn.Third,
        SuDokuCellValue.Nine
      )),
      singleValuesFifthBlock.isEmpty,
      singleValuesSixthBlock.isEmpty,
      singleValuesSeventhBlock.isEmpty,
      singleValuesEighthBlock == List((
        SuDokuRow.Seventh,
        SuDokuColumn.Fifth,
        SuDokuCellValue.Seven
      )),
      singleValuesNinthBlock == List((
        SuDokuRow.Ninth,
        SuDokuColumn.Eighth,
        SuDokuCellValue.Nine
      ))
    )

  pureTest(
    "SuDoku.blockSingleValueLoop should set all single values in a SuDoku puzzle"
  ):
    val (initialResult, initialSudoku) = SuDokuSolver.toSuDoku(
      "000000907\n" +
        "000420180\n" +
        "300705426\n" +
        "100904000\n" +
        "050000040\n" +
        "000507009\n" +
        "926108000\n" +
        "834059000\n" +
        "517000000"
    )

    val (result, sudoku) =
      blockSingleValueLoop(initialSudoku, SuDokuBlock.values.toList)

    val expectedSudoku =
      "|(2,4,6)      |(4,6,8)      |(1,2,5,8)    |(3,6,8)      |(1,3,6,8)    |(1,3,6)      |(9)          |(3,5)        |(7)          |\n" +
        "|(6,7)        |(6,7,9)      |(5)          |(4)          |(2)          |(3,6)        |(1)          |(8)          |(3,5)        |\n" +
        "|(3)          |(8)          |(1,8)        |(7)          |(9)          |(5)          |(4)          |(2)          |(6)          |\n" +
        "|(1)          |(6,7,8)      |(2,3,8)      |(9)          |(3,6,8)      |(4)          |(2,3,5,6,7,8)|(3,5,6,7)    |(2,3,5,8)    |\n" +
        "|(2,6,7)      |(5)          |(9)          |(2,3,6,8)    |(1,3,6,8)    |(1,2,3,6)    |(2,3,6,7,8)  |(4)          |(1,2,3,8)    |\n" +
        "|(2,4,6)      |(4,6,8)      |(2,3,8)      |(5)          |(1,3,6,8)    |(7)          |(2,3,6,8)    |(1,3,6)      |(9)          |\n" +
        "|(9)          |(2)          |(6)          |(1)          |(7)          |(8)          |(3,5)        |(3,5)        |(3,4,5)      |\n" +
        "|(8)          |(3)          |(4)          |(2,6)        |(5)          |(9)          |(2,6,7)      |(1,6,7)      |(1,2)        |\n" +
        "|(5)          |(1)          |(7)          |(2,3,6)      |(3,4,6)      |(2,3,6)      |(2,3,6,8)    |(9)          |(2,3,4,8)    |"

    expect.all(
      initialResult,
      result,
      sudoku.show == expectedSudoku
    )

  pureTest(
    "SuDoku.getSingleValues should return a correct list of the single values"
  ):
    val initialSudoku = SuDoku(
      Map(
        (
          SuDokuRow.Fourth,
          SuDokuColumn.Seventh,
          SuDokuBlock.Sixth
        ) -> SuDokuCell(Set(Nine)),
        (
          SuDokuRow.Fourth,
          SuDokuColumn.Eighth,
          SuDokuBlock.Sixth
        ) -> SuDokuCell(Set(Four, Seven, Three, Five)),
        (SuDokuRow.Fourth, SuDokuColumn.Ninth, SuDokuBlock.Sixth) -> SuDokuCell(
          Set(Four, Six, Seven, Five)
        ),
        (
          SuDokuRow.Fifth,
          SuDokuColumn.Seventh,
          SuDokuBlock.Sixth
        ) -> SuDokuCell(Set(One)),
        (SuDokuRow.Fifth, SuDokuColumn.Eighth, SuDokuBlock.Sixth) -> SuDokuCell(
          Set(Three)
        ),
        (SuDokuRow.Fifth, SuDokuColumn.Ninth, SuDokuBlock.Sixth) -> SuDokuCell(
          Set(Eight)
        ),
        (
          SuDokuRow.Sixth,
          SuDokuColumn.Seventh,
          SuDokuBlock.Sixth
        ) -> SuDokuCell(Set(Two)),
        (SuDokuRow.Sixth, SuDokuColumn.Eighth, SuDokuBlock.Sixth) -> SuDokuCell(
          Set(Four, Five)
        ),
        (SuDokuRow.Sixth, SuDokuColumn.Ninth, SuDokuBlock.Sixth) -> SuDokuCell(
          Set(Four, Five)
        )
      )
    )

    val singleValues =
      getSingleValues(initialSudoku, SuDokuBlock.Sixth)

    expect.all(
      singleValues.sorted == List(
        (SuDokuRow.Fourth, SuDokuColumn.Ninth, Six)
      ).sorted
    )

  pureTest(
    "SuDoku.getSingleValues should return a list of the single values in a big SuDoku puzzle"
  ):
    val initialSudoku = SuDoku(
      Map(
        (SuDokuRow.First, SuDokuColumn.First, SuDokuBlock.First) -> SuDokuCell(
          Set(Four, Five)
        ),
        (SuDokuRow.First, SuDokuColumn.Second, SuDokuBlock.First) -> SuDokuCell(
          Set(Four, Eight, Seven, Five)
        ),
        (SuDokuRow.First, SuDokuColumn.Third, SuDokuBlock.First) -> SuDokuCell(
          Set(Three)
        ),
        (
          SuDokuRow.First,
          SuDokuColumn.Fourth,
          SuDokuBlock.Second
        ) -> SuDokuCell(Set(Nine)),
        (SuDokuRow.First, SuDokuColumn.Fifth, SuDokuBlock.Second) -> SuDokuCell(
          Set(Two)
        ),
        (SuDokuRow.First, SuDokuColumn.Sixth, SuDokuBlock.Second) -> SuDokuCell(
          Set(One)
        ),
        (
          SuDokuRow.First,
          SuDokuColumn.Seventh,
          SuDokuBlock.Third
        ) -> SuDokuCell(Set(Six)),
        (SuDokuRow.First, SuDokuColumn.Eighth, SuDokuBlock.Third) -> SuDokuCell(
          Set(Seven, Five)
        ),
        (SuDokuRow.First, SuDokuColumn.Ninth, SuDokuBlock.Third) -> SuDokuCell(
          Set(Seven, Five)
        ),
        (SuDokuRow.Second, SuDokuColumn.First, SuDokuBlock.First) -> SuDokuCell(
          Set(Nine)
        ),
        (
          SuDokuRow.Second,
          SuDokuColumn.Second,
          SuDokuBlock.First
        ) -> SuDokuCell(Set(Six)),
        (SuDokuRow.Second, SuDokuColumn.Third, SuDokuBlock.First) -> SuDokuCell(
          Set(Four, Seven)
        ),
        (
          SuDokuRow.Second,
          SuDokuColumn.Fourth,
          SuDokuBlock.Second
        ) -> SuDokuCell(Set(Three)),
        (
          SuDokuRow.Second,
          SuDokuColumn.Fifth,
          SuDokuBlock.Second
        ) -> SuDokuCell(Set(Four)),
        (
          SuDokuRow.Second,
          SuDokuColumn.Sixth,
          SuDokuBlock.Second
        ) -> SuDokuCell(Set(Five)),
        (
          SuDokuRow.Second,
          SuDokuColumn.Seventh,
          SuDokuBlock.Third
        ) -> SuDokuCell(Set(Eight)),
        (
          SuDokuRow.Second,
          SuDokuColumn.Eighth,
          SuDokuBlock.Third
        ) -> SuDokuCell(Set(Two)),
        (SuDokuRow.Second, SuDokuColumn.Ninth, SuDokuBlock.Third) -> SuDokuCell(
          Set(One)
        ),
        (SuDokuRow.Third, SuDokuColumn.First, SuDokuBlock.First) -> SuDokuCell(
          Set(Two, Five)
        ),
        (SuDokuRow.Third, SuDokuColumn.Second, SuDokuBlock.First) -> SuDokuCell(
          Set(Five)
        ),
        (SuDokuRow.Third, SuDokuColumn.Third, SuDokuBlock.First) -> SuDokuCell(
          Set(One)
        ),
        (
          SuDokuRow.Third,
          SuDokuColumn.Fourth,
          SuDokuBlock.Second
        ) -> SuDokuCell(Set(Eight)),
        (SuDokuRow.Third, SuDokuColumn.Fifth, SuDokuBlock.Second) -> SuDokuCell(
          Set(Seven)
        ),
        (SuDokuRow.Third, SuDokuColumn.Sixth, SuDokuBlock.Second) -> SuDokuCell(
          Set(Six)
        ),
        (
          SuDokuRow.Third,
          SuDokuColumn.Seventh,
          SuDokuBlock.Third
        ) -> SuDokuCell(Set(Four)),
        (SuDokuRow.Third, SuDokuColumn.Eighth, SuDokuBlock.Third) -> SuDokuCell(
          Set(Nine, Three)
        ),
        (SuDokuRow.Third, SuDokuColumn.Ninth, SuDokuBlock.Third) -> SuDokuCell(
          Set(Three)
        ),
        (
          SuDokuRow.Fourth,
          SuDokuColumn.First,
          SuDokuBlock.Fourth
        ) -> SuDokuCell(Set(Three, Five)),
        (
          SuDokuRow.Fourth,
          SuDokuColumn.Second,
          SuDokuBlock.Fourth
        ) -> SuDokuCell(Set(Four, Three)),
        (
          SuDokuRow.Fourth,
          SuDokuColumn.Third,
          SuDokuBlock.Fourth
        ) -> SuDokuCell(Set(Eight)),
        (
          SuDokuRow.Fourth,
          SuDokuColumn.Fourth,
          SuDokuBlock.Fifth
        ) -> SuDokuCell(Set(One)),
        (SuDokuRow.Fourth, SuDokuColumn.Fifth, SuDokuBlock.Fifth) -> SuDokuCell(
          Set(Six, Three)
        ),
        (SuDokuRow.Fourth, SuDokuColumn.Sixth, SuDokuBlock.Fifth) -> SuDokuCell(
          Set(Two)
        ),
        (
          SuDokuRow.Fourth,
          SuDokuColumn.Seventh,
          SuDokuBlock.Sixth
        ) -> SuDokuCell(Set(Nine)),
        (
          SuDokuRow.Fourth,
          SuDokuColumn.Eighth,
          SuDokuBlock.Sixth
        ) -> SuDokuCell(Set(Four, Seven, Three, Five)),
        (SuDokuRow.Fourth, SuDokuColumn.Ninth, SuDokuBlock.Sixth) -> SuDokuCell(
          Set(Four, Six, Seven, Five)
        ),
        (SuDokuRow.Fifth, SuDokuColumn.First, SuDokuBlock.Fourth) -> SuDokuCell(
          Set(Seven)
        ),
        (
          SuDokuRow.Fifth,
          SuDokuColumn.Second,
          SuDokuBlock.Fourth
        ) -> SuDokuCell(Set(Two)),
        (SuDokuRow.Fifth, SuDokuColumn.Third, SuDokuBlock.Fourth) -> SuDokuCell(
          Set(Nine)
        ),
        (SuDokuRow.Fifth, SuDokuColumn.Fourth, SuDokuBlock.Fifth) -> SuDokuCell(
          Set(Five)
        ),
        (SuDokuRow.Fifth, SuDokuColumn.Fifth, SuDokuBlock.Fifth) -> SuDokuCell(
          Set(Six, Three)
        ),
        (SuDokuRow.Fifth, SuDokuColumn.Sixth, SuDokuBlock.Fifth) -> SuDokuCell(
          Set(Four)
        ),
        (
          SuDokuRow.Fifth,
          SuDokuColumn.Seventh,
          SuDokuBlock.Sixth
        ) -> SuDokuCell(Set(One)),
        (SuDokuRow.Fifth, SuDokuColumn.Eighth, SuDokuBlock.Sixth) -> SuDokuCell(
          Set(Three)
        ),
        (SuDokuRow.Fifth, SuDokuColumn.Ninth, SuDokuBlock.Sixth) -> SuDokuCell(
          Set(Eight)
        ),
        (SuDokuRow.Sixth, SuDokuColumn.First, SuDokuBlock.Fourth) -> SuDokuCell(
          Set(One, Three, Five)
        ),
        (
          SuDokuRow.Sixth,
          SuDokuColumn.Second,
          SuDokuBlock.Fourth
        ) -> SuDokuCell(Set(Four, One, Three)),
        (SuDokuRow.Sixth, SuDokuColumn.Third, SuDokuBlock.Fourth) -> SuDokuCell(
          Set(Six)
        ),
        (SuDokuRow.Sixth, SuDokuColumn.Fourth, SuDokuBlock.Fifth) -> SuDokuCell(
          Set(Seven)
        ),
        (SuDokuRow.Sixth, SuDokuColumn.Fifth, SuDokuBlock.Fifth) -> SuDokuCell(
          Set(Nine)
        ),
        (SuDokuRow.Sixth, SuDokuColumn.Sixth, SuDokuBlock.Fifth) -> SuDokuCell(
          Set(Eight)
        ),
        (
          SuDokuRow.Sixth,
          SuDokuColumn.Seventh,
          SuDokuBlock.Sixth
        ) -> SuDokuCell(Set(Two)),
        (SuDokuRow.Sixth, SuDokuColumn.Eighth, SuDokuBlock.Sixth) -> SuDokuCell(
          Set(Four, Five)
        ),
        (SuDokuRow.Sixth, SuDokuColumn.Ninth, SuDokuBlock.Sixth) -> SuDokuCell(
          Set(Four, Five)
        ),
        (
          SuDokuRow.Seventh,
          SuDokuColumn.First,
          SuDokuBlock.Seventh
        ) -> SuDokuCell(Set(One, Three)),
        (
          SuDokuRow.Seventh,
          SuDokuColumn.Second,
          SuDokuBlock.Seventh
        ) -> SuDokuCell(Set(Four, One, Seven, Three)),
        (
          SuDokuRow.Seventh,
          SuDokuColumn.Third,
          SuDokuBlock.Seventh
        ) -> SuDokuCell(Set(Two)),
        (
          SuDokuRow.Seventh,
          SuDokuColumn.Fourth,
          SuDokuBlock.Eighth
        ) -> SuDokuCell(Set(Six)),
        (
          SuDokuRow.Seventh,
          SuDokuColumn.Fifth,
          SuDokuBlock.Eighth
        ) -> SuDokuCell(Set(Eight)),
        (
          SuDokuRow.Seventh,
          SuDokuColumn.Sixth,
          SuDokuBlock.Eighth
        ) -> SuDokuCell(Set(Nine)),
        (
          SuDokuRow.Seventh,
          SuDokuColumn.Seventh,
          SuDokuBlock.Ninth
        ) -> SuDokuCell(Set(Five)),
        (
          SuDokuRow.Seventh,
          SuDokuColumn.Eighth,
          SuDokuBlock.Ninth
        ) -> SuDokuCell(Set(Four, One, Seven)),
        (
          SuDokuRow.Seventh,
          SuDokuColumn.Ninth,
          SuDokuBlock.Ninth
        ) -> SuDokuCell(Set(Four, Seven)),
        (
          SuDokuRow.Eighth,
          SuDokuColumn.First,
          SuDokuBlock.Seventh
        ) -> SuDokuCell(Set(Eight)),
        (
          SuDokuRow.Eighth,
          SuDokuColumn.Second,
          SuDokuBlock.Seventh
        ) -> SuDokuCell(Set(Four, One)),
        (
          SuDokuRow.Eighth,
          SuDokuColumn.Third,
          SuDokuBlock.Seventh
        ) -> SuDokuCell(Set(Four)),
        (
          SuDokuRow.Eighth,
          SuDokuColumn.Fourth,
          SuDokuBlock.Eighth
        ) -> SuDokuCell(Set(Two)),
        (
          SuDokuRow.Eighth,
          SuDokuColumn.Fifth,
          SuDokuBlock.Eighth
        ) -> SuDokuCell(Set(Five)),
        (
          SuDokuRow.Eighth,
          SuDokuColumn.Sixth,
          SuDokuBlock.Eighth
        ) -> SuDokuCell(Set(Three)),
        (
          SuDokuRow.Eighth,
          SuDokuColumn.Seventh,
          SuDokuBlock.Ninth
        ) -> SuDokuCell(Set(Seven)),
        (
          SuDokuRow.Eighth,
          SuDokuColumn.Eighth,
          SuDokuBlock.Ninth
        ) -> SuDokuCell(Set(Six)),
        (SuDokuRow.Eighth, SuDokuColumn.Ninth, SuDokuBlock.Ninth) -> SuDokuCell(
          Set(Nine)
        ),
        (
          SuDokuRow.Ninth,
          SuDokuColumn.First,
          SuDokuBlock.Seventh
        ) -> SuDokuCell(Set(Six)),
        (
          SuDokuRow.Ninth,
          SuDokuColumn.Second,
          SuDokuBlock.Seventh
        ) -> SuDokuCell(Set(Nine)),
        (
          SuDokuRow.Ninth,
          SuDokuColumn.Third,
          SuDokuBlock.Seventh
        ) -> SuDokuCell(Set(Five)),
        (
          SuDokuRow.Ninth,
          SuDokuColumn.Fourth,
          SuDokuBlock.Eighth
        ) -> SuDokuCell(Set(Four)),
        (SuDokuRow.Ninth, SuDokuColumn.Fifth, SuDokuBlock.Eighth) -> SuDokuCell(
          Set(One)
        ),
        (SuDokuRow.Ninth, SuDokuColumn.Sixth, SuDokuBlock.Eighth) -> SuDokuCell(
          Set(Seven)
        ),
        (
          SuDokuRow.Ninth,
          SuDokuColumn.Seventh,
          SuDokuBlock.Ninth
        ) -> SuDokuCell(Set(Three)),
        (SuDokuRow.Ninth, SuDokuColumn.Eighth, SuDokuBlock.Ninth) -> SuDokuCell(
          Set(Eight)
        ),
        (SuDokuRow.Ninth, SuDokuColumn.Ninth, SuDokuBlock.Ninth) -> SuDokuCell(
          Set(Two)
        )
      )
    )

    val singleValuesFirstBlock =
      getSingleValues(initialSudoku, SuDokuBlock.First)
    val singleValuesSecondBlock =
      getSingleValues(initialSudoku, SuDokuBlock.Second)
    val singleValuesThirdBlock =
      getSingleValues(initialSudoku, SuDokuBlock.Third)
    val singleValuesFourthBlock =
      getSingleValues(initialSudoku, SuDokuBlock.Fourth)
    val singleValuesFifthBlock =
      getSingleValues(initialSudoku, SuDokuBlock.Fifth)
    val singleValuesSixthBlock =
      getSingleValues(initialSudoku, SuDokuBlock.Sixth)
    val singleValuesSeventhBlock =
      getSingleValues(initialSudoku, SuDokuBlock.Seventh)
    val singleValuesEighthBlock =
      getSingleValues(initialSudoku, SuDokuBlock.Eighth)
    val singleValuesNinthBlock =
      getSingleValues(initialSudoku, SuDokuBlock.Ninth)

    expect.all(
      singleValuesFirstBlock.sorted == List(
        (SuDokuRow.First, SuDokuColumn.Second, Eight),
        (SuDokuRow.Third, SuDokuColumn.First, Two)
      ).sorted,
      singleValuesSecondBlock.isEmpty,
      singleValuesThirdBlock == List(
        (SuDokuRow.Third, SuDokuColumn.Eighth, Nine)
      ),
      singleValuesFourthBlock.isEmpty,
      singleValuesFifthBlock.isEmpty,
      singleValuesSixthBlock.sorted == List(
        (SuDokuRow.Fourth, SuDokuColumn.Ninth, Six)
      ).sorted,
      singleValuesSeventhBlock == List(
        (SuDokuRow.Seventh, SuDokuColumn.Second, Seven)
      ),
      singleValuesEighthBlock.isEmpty,
      singleValuesNinthBlock == List(
        (SuDokuRow.Seventh, SuDokuColumn.Eighth, One)
      )
    )

  pureTest(
    "SuDoku.getSolution should return the solution of the sudoku puzzle"
  ):
    val initialSudoku = SuDoku.initial
    val inputs: Seq[SuDokuInput] = Seq(
      (SuDokuRow.First, SuDokuColumn.Third, SuDokuCellValue.Three),
      (SuDokuRow.First, SuDokuColumn.Fifth, SuDokuCellValue.Two),
      (SuDokuRow.First, SuDokuColumn.Seventh, SuDokuCellValue.Six),
      (SuDokuRow.Second, SuDokuColumn.First, SuDokuCellValue.Nine),
      (SuDokuRow.Second, SuDokuColumn.Fourth, SuDokuCellValue.Three),
      (SuDokuRow.Second, SuDokuColumn.Sixth, SuDokuCellValue.Five),
      (SuDokuRow.Second, SuDokuColumn.Ninth, SuDokuCellValue.One),
      (SuDokuRow.Third, SuDokuColumn.Third, SuDokuCellValue.One),
      (SuDokuRow.Third, SuDokuColumn.Fourth, SuDokuCellValue.Eight),
      (SuDokuRow.Third, SuDokuColumn.Sixth, SuDokuCellValue.Six),
      (SuDokuRow.Third, SuDokuColumn.Seventh, SuDokuCellValue.Four),
      (SuDokuRow.Fourth, SuDokuColumn.Third, SuDokuCellValue.Eight),
      (SuDokuRow.Fourth, SuDokuColumn.Fourth, SuDokuCellValue.One),
      (SuDokuRow.Fourth, SuDokuColumn.Sixth, SuDokuCellValue.Two),
      (SuDokuRow.Fourth, SuDokuColumn.Seventh, SuDokuCellValue.Nine),
      (SuDokuRow.Fifth, SuDokuColumn.First, SuDokuCellValue.Seven),
      (SuDokuRow.Fifth, SuDokuColumn.Ninth, SuDokuCellValue.Eight),
      (SuDokuRow.Sixth, SuDokuColumn.Third, SuDokuCellValue.Six),
      (SuDokuRow.Sixth, SuDokuColumn.Fourth, SuDokuCellValue.Seven),
      (SuDokuRow.Sixth, SuDokuColumn.Sixth, SuDokuCellValue.Eight),
      (SuDokuRow.Sixth, SuDokuColumn.Seventh, SuDokuCellValue.Two),
      (SuDokuRow.Seventh, SuDokuColumn.Third, SuDokuCellValue.Two),
      (SuDokuRow.Seventh, SuDokuColumn.Fourth, SuDokuCellValue.Six),
      (SuDokuRow.Seventh, SuDokuColumn.Sixth, SuDokuCellValue.Nine),
      (SuDokuRow.Seventh, SuDokuColumn.Seventh, SuDokuCellValue.Five),
      (SuDokuRow.Eighth, SuDokuColumn.First, SuDokuCellValue.Eight),
      (SuDokuRow.Eighth, SuDokuColumn.Fourth, SuDokuCellValue.Two),
      (SuDokuRow.Eighth, SuDokuColumn.Sixth, SuDokuCellValue.Three),
      (SuDokuRow.Eighth, SuDokuColumn.Ninth, SuDokuCellValue.Nine),
      (SuDokuRow.Ninth, SuDokuColumn.Third, SuDokuCellValue.Five),
      (SuDokuRow.Ninth, SuDokuColumn.Fifth, SuDokuCellValue.One),
      (SuDokuRow.Ninth, SuDokuColumn.Seventh, SuDokuCellValue.Three)
    )

    val (_, sudoku) =
      SuDokuSolver.setValues(inputs*).run(initialSudoku)

    val (result, sudokuWithSolution) = SuDokuSolver.solutionState.run(sudoku)

    val expectedSudoku =
      "|(4)|(8)|(3)|(9)|(2)|(1)|(6)|(5)|(7)|\n" +
        "|(9)|(6)|(7)|(3)|(4)|(5)|(8)|(2)|(1)|\n" +
        "|(2)|(5)|(1)|(8)|(7)|(6)|(4)|(9)|(3)|\n" +
        "|(5)|(4)|(8)|(1)|(3)|(2)|(9)|(7)|(6)|\n" +
        "|(7)|(2)|(9)|(5)|(6)|(4)|(1)|(3)|(8)|\n" +
        "|(1)|(3)|(6)|(7)|(9)|(8)|(2)|(4)|(5)|\n" +
        "|(3)|(7)|(2)|(6)|(8)|(9)|(5)|(1)|(4)|\n" +
        "|(8)|(1)|(4)|(2)|(5)|(3)|(7)|(6)|(9)|\n" +
        "|(6)|(9)|(5)|(4)|(1)|(7)|(3)|(8)|(2)|"

    expect.all(
      result,
      sudokuWithSolution.show == expectedSudoku
    )

  pureTest("SuDoku.toSuDoku should convert a string to a SuDoku puzzle"):
    val (convertedResult, convertedSudoku) = SuDokuSolver.toSuDoku(
      "003020600\n" +
        "900305001\n" +
        "001806400\n" +
        "008102900\n" +
        "700000008\n" +
        "006708200\n" +
        "002609500\n" +
        "800203009\n" +
        "005010300"
    )
    val (solutionResult, sudokuWithSolution) =
      SuDokuSolver.solutionState.run(convertedSudoku)

    val expectedConvertedSudoku =
      "|(4,5)        |(4,5,7,8)    |(3)          |(4,9)        |(2)          |(1,4,7)      |(6)          |(5,7,8,9)    |(5,7)        |\n" +
        "|(9)          |(2,4,6,7,8)  |(4,7)        |(3)          |(4,7)        |(5)          |(7,8)        |(2,7,8)      |(1)          |\n" +
        "|(2,5)        |(2,5,7)      |(1)          |(8)          |(7,9)        |(6)          |(4)          |(2,3,5,7,9)  |(2,3,5,7)    |\n" +
        "|(3,4,5)      |(3,4,5)      |(8)          |(1)          |(3,4,5,6)    |(2)          |(9)          |(3,4,5,6,7)  |(3,4,5,6,7)  |\n" +
        "|(7)          |(1,2,3,4,5,9)|(4,9)        |(4,5,9)      |(3,4,5,6,9)  |(4)          |(1)          |(1,3,4,5,6)  |(8)          |\n" +
        "|(1,3,4,5)    |(1,3,4,5,9)  |(6)          |(7)          |(3,4,5,9)    |(8)          |(2)          |(1,3,4,5)    |(3,4,5)      |\n" +
        "|(1,3,4)      |(1,3,4,7)    |(2)          |(6)          |(4,7,8)      |(9)          |(5)          |(1,4,7,8)    |(4,7)        |\n" +
        "|(8)          |(1,4,6,7)    |(4,7)        |(2)          |(4,5,7)      |(3)          |(1,7)        |(1,4,6,7)    |(9)          |\n" +
        "|(4,6)        |(4,6,7,9)    |(5)          |(4)          |(1)          |(4,7)        |(3)          |(2,4,6,7,8)  |(2,4,6,7)    |"

    val expectedSudokuWithSolution =
      "|(4)|(8)|(3)|(9)|(2)|(1)|(6)|(5)|(7)|\n" +
        "|(9)|(6)|(7)|(3)|(4)|(5)|(8)|(2)|(1)|\n" +
        "|(2)|(5)|(1)|(8)|(7)|(6)|(4)|(9)|(3)|\n" +
        "|(5)|(4)|(8)|(1)|(3)|(2)|(9)|(7)|(6)|\n" +
        "|(7)|(2)|(9)|(5)|(6)|(4)|(1)|(3)|(8)|\n" +
        "|(1)|(3)|(6)|(7)|(9)|(8)|(2)|(4)|(5)|\n" +
        "|(3)|(7)|(2)|(6)|(8)|(9)|(5)|(1)|(4)|\n" +
        "|(8)|(1)|(4)|(2)|(5)|(3)|(7)|(6)|(9)|\n" +
        "|(6)|(9)|(5)|(4)|(1)|(7)|(3)|(8)|(2)|"

    expect.all(
      convertedResult,
      convertedSudoku.show == expectedConvertedSudoku,
      solutionResult,
      sudokuWithSolution.show == expectedSudokuWithSolution
    )

  pureTest("SuDoku.toSuDoku should convert a string to a SuDoku puzzle"):
    val (convertedResult, convertedSudoku) = SuDokuSolver.toSuDoku(
      "000000907\n" +
        "000420180\n" +
        "000705026\n" +
        "100904000\n" +
        "050000040\n" +
        "000507009\n" +
        "920108000\n" +
        "034059000\n" +
        "507000000"
    )
    val (solutionResult, sudokuWithSolution) =
      SuDokuSolver.solutionState.run(convertedSudoku)

    val expectedSudokuWithSolution =
      "|(4)|(6)|(2)|(8)|(3)|(1)|(9)|(5)|(7)|\n" +
        "|(7)|(9)|(5)|(4)|(2)|(6)|(1)|(8)|(3)|\n" +
        "|(3)|(8)|(1)|(7)|(9)|(5)|(4)|(2)|(6)|\n" +
        "|(1)|(7)|(3)|(9)|(8)|(4)|(2)|(6)|(5)|\n" +
        "|(6)|(5)|(9)|(3)|(1)|(2)|(7)|(4)|(8)|\n" +
        "|(2)|(4)|(8)|(5)|(6)|(7)|(3)|(1)|(9)|\n" +
        "|(9)|(2)|(6)|(1)|(7)|(8)|(5)|(3)|(4)|\n" +
        "|(8)|(3)|(4)|(2)|(5)|(9)|(6)|(7)|(1)|\n" +
        "|(5)|(1)|(7)|(6)|(4)|(3)|(8)|(9)|(2)|"

    expect.all(
      convertedResult,
      solutionResult,
      sudokuWithSolution.show == expectedSudokuWithSolution
    )
end SuDokuSolverSuite
