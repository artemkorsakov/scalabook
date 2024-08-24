package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDoku.*
import ru.scalabook.algorithms.games.sudoku.SuDokuComplexInner.{
  SuDokuComplexInnerInput,
  SuDokuInnerComplexOperation
}
import ru.scalabook.algorithms.games.sudoku.SuDokuInner.{
  SuDokuInnerInput,
  SuDokuInnerOperation
}
import ru.scalabook.fp.state.State
import scala.annotation.tailrec
import scala.util.Try

final case class SuDoku(cells: Vector[Vector[SuDokuCell]]):
  val isCorrect: Boolean =
    cells.length == 9 &&
      cells.forall(_.length == 9) &&
      cells.forall(_.forall(_.possibleValues.nonEmpty)) &&
      cells.forall(row => hasUniqueValues(row)) &&
      (0 to 8).forall(j => hasUniqueValues(cells.map(row => row(j)))) && {
        val blocks =
          for
            i <- 0 to 6 by 3
            j <- 0 to 6 by 3
          yield getBlock(i, j)
        blocks.forall: block =>
          val blockCells = block.map((i, j) => cells(i.toInt)(j.toInt))
          hasUniqueValues(blockCells)
      }

  val hasSolution: Boolean =
    isCorrect && cells.forall(_.forall(_.maybeValue.isDefined))

object SuDoku:
  val initial: SuDoku =
    val defaultCells =
      (0 to 8)
        .toVector
        .map: _ =>
          (0 to 8).toVector.map: _ =>
            SuDokuCell.initial

    SuDoku(defaultCells)

  private def hasUniqueValues(cells: IndexedSeq[SuDokuCell]): Boolean =
    val values = cells.flatMap(_.maybeValue)
    values.distinct.size == values.size

  type Row = Int
  object Row:
    def isCorrect(row: Int): Boolean = 0 <= row && row < 9
  type Column = Int
  object Column:
    def isCorrect(col: Int): Boolean = 0 <= col && col < 9

  def getBlock(i: Row, j: Column): IndexedSeq[(Row, Column)] =
    if Row.isCorrect(i) && Column.isCorrect(j) then
      for
        k <- 0 to 2
        l <- 0 to 2
      yield
        val r = (i / 3) * 3 + k
        val c = (j / 3) * 3 + l
        (r, c)
    else IndexedSeq.empty

  type SuDokuState  = State[SuDoku, Boolean]
  type SuDokuOutput = (Boolean, SuDoku)

  def executeInnerOperation[A](
      inputs: List[A],
      update: A => SuDokuOutput => SuDokuOutput
  ): SuDokuState =
    State[SuDoku, Boolean] { sudoku =>
      val state =
        for
          _ <- State.traverse(inputs)(input =>
            State.modify[SuDokuOutput](update(input))
          )
          output <- State.get
        yield output._1

      state.run((true, sudoku))._2
    }

  type SuDokuInput =
    (Row, Column, SuDokuCellValue)

  private val updateSuDoku: SuDokuInput => SuDokuOutput => SuDokuOutput =
    (i, j, value) =>
      (result, sudoku) =>
        val inputSet: SuDokuInnerInput =
          (SuDokuInnerOperation.SetToCell, i, j, value)
        val (resultSet, sudokuSet) =
          SuDokuInner.execute(inputSet).run(sudoku)

        val inputsRemove: List[SuDokuComplexInnerInput] =
          List(
            (SuDokuInnerComplexOperation.RemoveFromRow, i, j, value),
            (SuDokuInnerComplexOperation.RemoveFromColumn, i, j, value),
            (SuDokuInnerComplexOperation.RemoveFromBlock, i, j, value)
          )
        val (resultRemove, sudokuRemove) =
          SuDokuComplexInner.execute(inputsRemove).run(sudokuSet)

        if result && resultSet && resultRemove then (true, sudokuRemove)
        else (false, sudoku)

  /** Устанавливает заданное значение в ячейку. */
  def setValue(i: Int, j: Int, value: SuDokuCellValue): SuDokuState =
    setValues((i, j, value))

  /** Устанавливает заданные значения в пачку ячеек. */
  def setValues(inputs: SuDokuInput*): SuDokuState =
    SuDoku.executeInnerOperation(inputs.toList, updateSuDoku)

  /** Поиск значений, которые можно вычеркнуть:
    *
    *   - проходимся по каждой строчке и по каждому столбцу
    *   - в каждой ячейке ищем среди списка возможных такие значения, которые
    *     при вставке в заданное поле приводят к ошибке - это означает, что в
    *     данную ячейку такое значение вставлять нельзя и его можно вычеркнуть
    *   - вычеркиваем значение
    *   - пытаемся найти следующий кандидат на вычеркивание в той же ячейке
    *   - если в ячейке не осталось значений, которые можно вычеркнуть, то
    *     переходим к следующей ячейке
    */
  @tailrec
  private def crossOutLoop(sudoku: SuDoku, i: Int, j: Int): SuDokuOutput =
    // Дошли до конца поля
    if i >= 9 then (true, sudoku)
    // Дошли до конца столбца, переходим к следующей строке
    else if j >= 9 then crossOutLoop(sudoku, i + 1, 0)
    else
      val currentValues = sudoku.cells(i)(j).possibleValues
      // Точное значение определено, переходим к следующей ячейке
      if currentValues.size == 1 then
        crossOutLoop(sudoku, i, j + 1)
      else // Ищем значение в ячейке, которое можно вычеркнуть
        val maybeInvalidValue = currentValues.find: value =>
          !setValue(i, j, value).run(sudoku)._1
        maybeInvalidValue match
          // Такого значения нет, переходим к следующей ячейке
          case None => crossOutLoop(sudoku, i, j + 1)
          case Some(invalidValue) =>
            val input: SuDokuInnerInput =
              (SuDokuInnerOperation.RemoveFromCell, i, j, invalidValue)
            val (result, newSudoku) =
              SuDokuInner.execute(input).run(sudoku)
            // Если результат вычеркивания успешен,
            // то ищем в заданной ячейке следующий кандидат на вычеркивание
            if result then crossOutLoop(newSudoku, i, j)
            // Если не успешен, значит решения головоломки не существует
            else (false, sudoku)
      end if

//  @tailrec
//  private def blockSingleValueLoop(
//      sudoku: SuDoku,
//      i: Int,
//      j: Int
//  ): SuDokuOutput =
//    // Дошли до конца поля
//    if i >= 9 then (true, sudoku)
//    // Дошли до конца столбца, переходим к следующему блоку
//    else if j >= 9 then blockSingleValueLoop(sudoku, i + 3, 0)
//    else
//      val block        = getBlock(i, j)
//      val singleValues = getSingleValues(sudoku, block)
//      if singleValues.isEmpty then blockSingleValueLoop(sudoku, i, j + 3)
//      else
//        val (result, sudoku2) = setValues(singleValues*).run(sudoku)
//        if result then blockSingleValueLoop(sudoku2, i, j + 3)
//        else (false, sudoku)
//
//  /** Находим такую ячейку и такие значения, которые не встречаются в других
//    * ячейках блока, но которые не единственные в своих ячейках
//    */
//  private def getSingleValues(
//      sudoku: SuDoku,
//      block: IndexedSeq[(Row, Column)]
//  ) =
//    block.foldLeft(List.empty[SuDokuInput]) {
//      case (acc, (i, j)) =>
//        val values = sudoku.cells(i)(j).possibleValues
//        val maybeSingleValue =
//          if values.size == 1 then None
//          else
//            values.find: value =>
//              block.forall: (ii, jj) =>
//                (ii == i && jj == j) || !sudoku.cells(ii)(jj).possibleValues
//                  .contains(value)
//        maybeSingleValue match
//          case None        => acc
//          case Some(value) => (i, j, value) :: acc
//    }

  /** Головоломка решается до тех пор, пока:
    *
    *   - не найдено решение
    *   - если есть значения, которые можно вычеркнуть
    */
  def getSolution: SuDokuState =
    State[SuDoku, Boolean] {
      (sudoku: SuDoku) =>

        @tailrec
        def loop(sudoku: SuDoku): SuDokuOutput =
          val (result, newSudoku) = crossOutLoop(sudoku, 0, 0)
          if result && sudoku == newSudoku then
            // Ничего не изменилось, не можем найти решение
            (result, sudoku)
          else if result then
            loop(newSudoku)
          else (false, sudoku)

        loop(sudoku)
    }

  def toSuDoku(source: String): SuDokuOutput =
    val rows = source.trim.split("\n")

    def getValue(i: Int, j: Int): Option[SuDokuCellValue] =
      if 0 <= i && i < rows.length && 0 <= j && j < rows(i).length then
        val value = rows(i).charAt(j).getNumericValue
        if 1 <= value && value <= 9 then
          Try(SuDokuCellValue.fromOrdinal(value - 1)).toOption
        else None
      else None

    val inputs: IndexedSeq[SuDokuInput] =
      for
        i     <- 0 to 8
        j     <- 0 to 8
        value <- getValue(i, j)
      yield (i, j, value)

    setValues(inputs*).run(SuDoku.initial)

  end toSuDoku
end SuDoku
