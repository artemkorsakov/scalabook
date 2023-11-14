package ru.scalabook.algorithms.equations

import weaver.FunSuite

object DiophantineEquationSuite extends FunSuite:
  test("minimalEquation and isSuccess"):
    expect.all(
      DiophantineEquation(67).minimalEquation
        .contains((BigInt(48842), BigInt(5967))),
      DiophantineEquation(13).minimalEquation
        .contains((BigInt(649), BigInt(180))),
      DiophantineEquation(2).minimalEquation.contains((BigInt(3), BigInt(2))),
      DiophantineEquation(3).minimalEquation.contains((BigInt(2), BigInt(1))),
      DiophantineEquation(5).minimalEquation.contains((BigInt(9), BigInt(4))),
      DiophantineEquation(6).minimalEquation.contains((BigInt(5), BigInt(2))),
      DiophantineEquation(7).minimalEquation.contains((BigInt(8), BigInt(3))),
      DiophantineEquation(4).minimalEquation.isEmpty,
      DiophantineEquation(9).minimalEquation.isEmpty,
      DiophantineEquation(61).minimalEquation
        .contains((BigInt(1766319049), BigInt(226153980))),
      DiophantineEquation(73).minimalEquation
        .contains((BigInt(2281249), BigInt(267000))),
      DiophantineEquation(94).minimalEquation
        .contains((BigInt(2143295), BigInt(221064))),
      DiophantineEquation(109).minimalEquation
        .contains((BigInt(158070671986249L), BigInt(15140424455100L))),
      DiophantineEquation(110).minimalEquation
        .contains((BigInt(21), BigInt(2))),
      DiophantineEquation(61).isSuccess(BigInt(1766319049), BigInt(226153980))
    )
