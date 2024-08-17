package ru.scalabook.algorithms.games

import cats.Show
import org.scalacheck.{Arbitrary, Gen}

package object sudoku:
  given Show[SuDokuCellValue] = Show.show[SuDokuCellValue]:
    (value: SuDokuCellValue) =>
      (value.ordinal + 1).toString
  private val suDokuCellValueGen: Gen[SuDokuCellValue] =
    Gen.oneOf(SuDokuCellValue.values.toSeq)
  given Arbitrary[SuDokuCellValue] = Arbitrary(suDokuCellValueGen)

  given Show[SuDokuCell] = Show.show[SuDokuCell]: (cell: SuDokuCell) =>
    cell.possibleValues.map(v => Show[SuDokuCellValue].show(v)).toList.sorted
      .mkString("(", ",", ")")

  opaque type FieldLength = Int
  private val fieldLengthGen: Gen[FieldLength] =
    Gen.choose(0, 8)
  given Arbitrary[FieldLength] = Arbitrary(fieldLengthGen)
  given Show[FieldLength] = Show.fromToString
  extension (i: FieldLength)
    def toInt: Int = i

  opaque type InvalidFieldLength = Int
  private val invalidFieldLengthGen: Gen[InvalidFieldLength] =
    Gen.oneOf(Gen.negNum[Int], Gen.choose(9, Int.MaxValue))
  given Arbitrary[InvalidFieldLength] = Arbitrary(invalidFieldLengthGen)
  given Show[InvalidFieldLength] = Show.fromToString
  extension (i: InvalidFieldLength)
    def toInvalidInt: Int = i

/*object SuDokuGenerators:
  given Show[SuDoku] = Show.show[SuDoku]: (field: SuDoku) =>
    field.cells.map: row =>
      row.map(_.show).mkString("[", ";", "]")
    .mkString("{", " ", "}")


end SuDokuGenerators
 */
