import ru.scalabook.algorithms.games.sudoku.SuDokuCellValue
import ru.scalabook.algorithms.games.sudoku.SuDokuCellValue.*
import ru.scalabook.algorithms.games.sudoku.SuDokuCell
import ru.scalabook.algorithms.games.sudoku.SuDokuCell.*
import ru.scalabook.algorithms.games.sudoku.SuDokuInner
import ru.scalabook.algorithms.games.sudoku.SuDokuInner.*
import ru.scalabook.algorithms.games.sudoku.SuDokuComplexInner
import ru.scalabook.algorithms.games.sudoku.SuDokuComplexInner.*
import ru.scalabook.algorithms.games.sudoku.SuDoku
import ru.scalabook.algorithms.games.sudoku.SuDoku.*
import ru.scalabook.fp.state.State

val initialSudoku = SuDoku.initial
val set: SuDokuInnerInput =
  (SuDokuInnerOperation.SetToCell, 1, 2, SuDokuCellValue.Six)
val (_, sudoku0) = SuDokuInner.execute(set).run(initialSudoku)
sudoku0.cells(1)(2)

val remove: SuDokuInnerInput =
  (SuDokuInnerOperation.RemoveFromCell, 2, 1, SuDokuCellValue.Five)
val (_, sudoku1) = SuDokuInner.execute(remove).run(initialSudoku)
sudoku1.cells(2)(1)
