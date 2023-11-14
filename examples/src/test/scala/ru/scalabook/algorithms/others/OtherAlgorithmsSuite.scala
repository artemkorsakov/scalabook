package ru.scalabook.algorithms.others

import ru.scalabook.algorithms.others.OtherAlgorithms.*
import weaver.FunSuite

object OtherAlgorithmsSuite extends FunSuite:
  test("Ackermann function - testing on small values"):
    expect.all(
      functionAckermann(1, 10) == 1024,
      functionAckermann(2, 4) == 65536,
      functionAckermann(3, 3) == 65536
    )
