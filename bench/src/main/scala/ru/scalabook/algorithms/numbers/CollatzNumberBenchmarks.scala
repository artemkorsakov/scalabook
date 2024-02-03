package ru.scalabook.algorithms.numbers

private val collatzNumber = CollatzNumber(1_000_000)

@main def collatzNumberBench(): Unit =
  (1 to 10_000).foreach: i =>
    collatzNumber.collatz(i)
