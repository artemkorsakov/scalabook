package ru.scalabook.algorithms.games.sudoku

import ru.scalabook.algorithms.games.sudoku.SuDokuOld.toSuDoku

@main def sudokuBench(): Unit =
  val maybeSuDoku = toSuDoku(
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

  maybeSuDoku.foreach: sudoku =>
    sudoku.result.foreach: result =>
      result.foreach(row => println(row.mkString("Array(", ", ", ")")))
