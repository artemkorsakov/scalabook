package ru.scalabook.algorithms.games.sudoku

@main def sudokuBench(): Unit =
  val (_, sudoku) = SuDoku.toSuDoku(
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
  val (_, sudokuWithSolution) = SuDoku.getSolution.run(sudoku)

  println(s"Sudoku: $sudokuWithSolution")
