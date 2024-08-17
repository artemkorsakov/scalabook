package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDokuOld.*
import weaver.FunSuite

object SuDokuOldSuite extends FunSuite:
  private val suDoku = toSuDoku(
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

  private val result: Array[Array[Int]] =
    suDoku.flatMap(_.result).getOrElse(Array.empty[Array[Int]])

  test("SuDoku.result"):
    expect.all(
      result(0).toSeq == Array(4, 8, 3, 9, 2, 1, 6, 5, 7).toSeq,
      result(1).toSeq == Array(9, 6, 7, 3, 4, 5, 8, 2, 1).toSeq,
      result(2).toSeq == Array(2, 5, 1, 8, 7, 6, 4, 9, 3).toSeq,
      result(3).toSeq == Array(5, 4, 8, 1, 3, 2, 9, 7, 6).toSeq,
      result(4).toSeq == Array(7, 2, 9, 5, 6, 4, 1, 3, 8).toSeq,
      result(5).toSeq == Array(1, 3, 6, 7, 9, 8, 2, 4, 5).toSeq,
      result(6).toSeq == Array(3, 7, 2, 6, 8, 9, 5, 1, 4).toSeq,
      result(7).toSeq == Array(8, 1, 4, 2, 5, 3, 7, 6, 9).toSeq,
      result(8).toSeq == Array(6, 9, 5, 4, 1, 7, 3, 8, 2).toSeq
    )
end SuDokuOldSuite

/*

import cats.Show
import cats.syntax.show.*
import org.scalacheck.{Arbitrary, Gen}
import ru.gitflic.artemkorsakov.problems76_100.SuDokuCellValue.{*, given}
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object SuDokuGenerators:
  given Show[SuDoku] = Show.show[SuDoku]: (field: SuDoku) =>
    field.cells.map: row =>
      row.map(_.show).mkString("[", ";", "]")
    .mkString("{", " ", "}")

  opaque type FieldLength = Int
  private val fieldLengthGen: Gen[FieldLength] =
    Gen.choose(0, 8)
  given Arbitrary[FieldLength] = Arbitrary(fieldLengthGen)
  given Show[FieldLength]      = Show.fromToString
  extension (i: FieldLength)
    def toInt: Int = i

  opaque type InvalidFieldLength = Int
  private val invalidFieldLengthGen: Gen[InvalidFieldLength] =
    Gen.oneOf(Gen.negNum[Int], Gen.choose(9, Int.MaxValue))
  given Arbitrary[InvalidFieldLength] = Arbitrary(invalidFieldLengthGen)
  given Show[InvalidFieldLength]      = Show.fromToString
  extension (i: InvalidFieldLength)
    def toInvalidInt: Int = i
end SuDokuGenerators

object Problem96Spec extends SimpleIOSuite with Checkers:
  test("removeValue should remove a value from an existing cell"):
    forall: (cellValue: SuDokuCellValue, i: FieldLength, j: FieldLength) =>
      val initialCell  = SuDokuCell(Set(cellValue))
      val initialCells = SuDoku().cells
      val sudoku =
        SuDoku(initialCells.updated(
          i.toInt,
          initialCells(i.toInt).updated(j.toInt, initialCell)
        ))
      val result = sudoku.removeValue(i.toInt, j.toInt, cellValue)
      expect(result.isEmpty)

  test("removeValue should return None if the cell does not exist"):
    forall:
      (
          cellValue: SuDokuCellValue,
          i: InvalidFieldLength,
          j: InvalidFieldLength
      ) =>
        val sudoku = SuDoku()
        val result =
          sudoku.removeValue(i.toInvalidInt, j.toInvalidInt, cellValue)
        expect(result.isEmpty)

  test(
    "removeValue should return a new SuDoku with the updated cell if the value is removed"
  ):
    forall:
      (
          cellValue: SuDokuCellValue,
          i: FieldLength,
          j: FieldLength
      ) =>
        val initialSudoku = SuDoku()
        val result = initialSudoku.removeValue(i.toInt, j.toInt, cellValue)
        whenSuccess(result): sudoku =>
          val cell = sudoku.cells(i.toInt)(j.toInt)
          expect.all(
            !cell.possibleValues.contains(cellValue)
          )

  test(
    "removeValue should not modify the original SuDoku if the value is not removed"
  ):
    forall: (cellValue: SuDokuCellValue, i: FieldLength, j: FieldLength) =>
      val initialCell  = SuDokuCell(Set(cellValue))
      val initialCells = SuDoku().cells
      val sudoku =
        SuDoku(initialCells.updated(
          i.toInt,
          initialCells(i.toInt).updated(j.toInt, initialCell)
        ))
      val result = sudoku.removeValue(i.toInt, j.toInt, cellValue)
      expect(result.isEmpty)

  pureTest(
    "removeValueFromRow should remove value from row, column, and square for the example"
  ):
    val maybeSuDoku = SuDoku().removeValueFromRow(2, 1, One)

    val initialCell =
      SuDokuCell(Set(One, Two, Three, Four, Five, Six, Seven, Eight, Nine))
    val cellWithoutOne =
      SuDokuCell(Set(Two, Three, Four, Five, Six, Seven, Eight, Nine))
    val initialRow = Vector(
      initialCell,
      initialCell,
      initialCell,
      initialCell,
      initialCell,
      initialCell,
      initialCell,
      initialCell,
      initialCell
    )

    whenSuccess(maybeSuDoku): suDoku =>
      expect.same(
        SuDoku(
          Vector(
            initialRow,
            initialRow,
            Vector(
              cellWithoutOne,
              initialCell,
              cellWithoutOne,
              cellWithoutOne,
              cellWithoutOne,
              cellWithoutOne,
              cellWithoutOne,
              cellWithoutOne,
              cellWithoutOne
            ),
            initialRow,
            initialRow,
            initialRow,
            initialRow,
            initialRow,
            initialRow
          )
        ),
        suDoku
      )
//
//  test("removeValueFromRow should remove value from row, column, and square"):
//    forall: (cellValue: SuDokuCellValue, i: FieldLength, j: FieldLength) =>
//      val maybeSuDoku =
//        SuDoku().removeValueFromRow(i.toInt, j.toInt, cellValue)
//
//      whenSuccess(maybeSuDoku): suDoku =>
//        forEach((0 to 8).toVector): (r: Int) =>
//          forEach((0 to 8).toVector): (c: Int) =>
//            if r == i.toInt && c == j.toInt then
//              expect.same(
//                SuDokuCellValue.values.toSet,
//                suDoku.cells(r)(c).possibleValues
//              )
//            else if r == i.toInt then success
//            else if c == j.toInt then success
//            else success
//
//  pureTest(
//    "removeValueFromRowColSqr should remove value from row, column, and square for the example"
//  ):
//    val maybeSuDoku = SuDoku().removeValueFromRowColSqr(2, 1, One)
//
//    val initialCell =
//      SuDokuCell(Set(One, Two, Three, Four, Five, Six, Seven, Eight, Nine))
//    val cellWithoutOne =
//      SuDokuCell(Set(Two, Three, Four, Five, Six, Seven, Eight, Nine))
//    whenSuccess(maybeSuDoku): suDoku =>
//      expect.same(
//        Vector(
//          Vector(
//            cellWithoutOne,
//            cellWithoutOne,
//            cellWithoutOne,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell
//          ),
//          Vector(
//            cellWithoutOne,
//            cellWithoutOne,
//            cellWithoutOne,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell
//          ),
//          Vector(
//            cellWithoutOne,
//            initialCell,
//            cellWithoutOne,
//            cellWithoutOne,
//            cellWithoutOne,
//            cellWithoutOne,
//            cellWithoutOne,
//            cellWithoutOne,
//            cellWithoutOne
//          ),
//          Vector(
//            initialCell,
//            cellWithoutOne,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell
//          ),
//          Vector(
//            initialCell,
//            cellWithoutOne,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell
//          ),
//          Vector(
//            initialCell,
//            cellWithoutOne,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell
//          ),
//          Vector(
//            initialCell,
//            cellWithoutOne,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell
//          ),
//          Vector(
//            initialCell,
//            cellWithoutOne,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell
//          ),
//          Vector(
//            initialCell,
//            cellWithoutOne,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell,
//            initialCell
//          )
//        ),
//        suDoku.cells
//      )
//
//  test(
//    "removeValueFromRowColSqr should remove value from row, column, and square"
//  ):
//    forall: (cellValue: SuDokuCellValue, i: FieldLength, j: FieldLength) =>
//      val maybeSuDoku =
//        SuDoku().removeValueFromRowColSqr(i.toInt, j.toInt, cellValue)
//
//      whenSuccess(maybeSuDoku): suDoku =>
//        forEach((0 to 8).toVector): (r: Int) =>
//          forEach((0 to 8).toVector): (c: Int) =>
//            if r == i.toInt && c == j.toInt then
//              expect.same(
//                SuDokuCellValue.values.toSet,
//                suDoku.cells(r)(c).possibleValues
//              )
//            else if r == i.toInt then success
//            else if c == j.toInt then success
//            else success

end Problem96Spec
 */
