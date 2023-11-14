package ru.scalabook.algorithms.matrix

import ru.scalabook.algorithms.matrix.Vector.*
import weaver.FunSuite

object VectorSuite extends FunSuite:
  test("norm"):
    expect.all(Vector(Seq(1, 2, -3, 1, 1)).norm == 4.0)

  test("add"):
    expect.all(
      Vector(Seq(1, 2, -3)) + Seq(-7, 4, 6) == Seq(-6, 6, 3),
      Vector(Seq(1L, 2L, -3L)) + Seq(-7L, 4L, 6L) == Seq(-6L, 6L, 3L),
      Vector(
        Seq(BigInt(156744), BigInt(53453535), BigInt(-656464646))
      ) + Seq(
        BigInt(-4324344),
        BigInt(455455455),
        BigInt(445354354)
      ) ==
        Seq(BigInt(-4167600), BigInt(508908990), BigInt(-211110292))
    )

  test("mul number"):
    expect.all(Vector(Seq(1, 2, -3)).*(5) == Seq(5, 10, -15))

  test("mul"):
    expect.all(
      Vector(Seq(1, 2, -3)).*(Seq(-7, 4, 6)) == -17,
      Vector(Seq(1, 2, -3)) * Seq(-7, 4, 6) == -17,
      Vector(Seq(1L, 2L, -3L)).*(Seq(-7L, 4L, 6L)) == -17L,
      Vector(Seq(1L, 2L, -3L)) * Seq(-7L, 4L, 6L) == -17L,
      Vector(Seq(BigInt(156744), BigInt(53453535), BigInt(-656464646)))
        .*(Seq(BigInt(-4324344), BigInt(455455455), BigInt(445354354))) ==
        BigInt(-268014362053361195L),
      Vector(
        Seq(BigInt(156744), BigInt(53453535), BigInt(-656464646))
      ) * Seq(
        BigInt(-4324344),
        BigInt(455455455),
        BigInt(445354354)
      ) ==
        BigInt(-268014362053361195L)
    )

  test("mulMod"):
    expect.all(
      Vector(Seq(1007, 2456, -3466)).*(Seq(-3347, 4343, 6445), 1000) == 609,
      Vector(Seq(1007L, 2456L, -3466L))
        .*(Seq(-3347L, 4343L, 6445L), 1000L) == 609L,
      Vector(Seq(BigInt(1007), BigInt(2456), BigInt(-3466)))
        .*(
          Seq(BigInt(-3347), BigInt(4343), BigInt(6445)),
          BigInt(1000)
        ) == BigInt(609)
    )

  test("toMatrix"):
    expect.all(
      Vector(Seq(1007, 2456, -3466)).rowToMatrix ==
        Matrix(Seq(Seq(1007, 2456, -3466)))
    )

  private val matrixA = Vector(Seq(1, -3, 6))
  private val matrixB = Matrix(
    Seq(Seq(3, 4, 2, 5), Seq(0, -1, 3, 2), Seq(1, 2, 3, 0))
  )
  private val matrixC = Seq(9, 19, 11, -1)

  test("matrix multiplication by row"):
    expect.all(
      matrixA.*(matrixB) == matrixC,
      matrixA.*(matrixB, 7) == Seq(2, 5, 4, 6)
    )

  test("distance"):
    expect.all(
      Vector(Seq(1, 2, -3)).distance(Seq(-1, 5, -2)) ==
        3.7416573867739413
    )

  test("distance"):
    expect.all(
      math.abs(
        Vector(Seq(1, 2, -3)).cos(Seq(-1, 5, -2)) - 0.7319250547113999
      ) <= 0.001
    )

  test("isOrthogonal"):
    expect.all(
      Vector(Seq(1, 2, -3)).isOrthogonal(Seq(-16, 5, -2)),
      !Vector(Seq(1, 2, -3)).isOrthogonal(Seq(-15, 5, -2))
    )

  test("manhattanMetric"):
    expect.all(Vector(Seq(1, 2, -3)).manhattanMetric(Seq(-1, 5, -2)) == 6)
