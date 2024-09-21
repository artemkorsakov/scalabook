package ru.scalabook.algorithms.derivative

import weaver.FunSuite

import ru.scalabook.algorithms.derivative.Derivative.Expression.*
import ru.scalabook.algorithms.derivative.Derivative.Operation.*

object DerivativeSuite extends FunSuite:
  private val x: Variable = Variable("x")
  private val y: Variable = Variable("y")

  test(
    "Derivative.isNumber корректно идентифицирует строки, содержащие только целые числа"
  ):
    expect.all(
      Derivative.isNumber("123"),
      !Derivative.isNumber("123a"),
      !Derivative.isNumber("a123"),
      !Derivative.isNumber("123.45"),
      !Derivative.isNumber("123.45.67"),
      !Derivative.isNumber("123a45")
    )

  test("Derivative.isVariable корректно идентифицирует переменные"):
    expect.all(
      Derivative.isVariable("x"),
      Derivative.isVariable("y"),
      !Derivative.isVariable("123"),
      !Derivative.isVariable("123x"),
      !Derivative.isVariable("x123"),
      !Derivative.isVariable("xy"),
      !Derivative.isVariable("")
    )

  test("Derivative.isSum корректно идентифицирует суммы"):
    expect.all(
      Derivative.isSum("2+x"),
      Derivative.isSum("x+2"),
      Derivative.isSum("x+y"),
      Derivative.isSum("2 + x"),
      Derivative.isSum("x +2"),
      Derivative.isSum("x+ y"),
      !Derivative.isSum("2*x"),
      !Derivative.isSum("x*2"),
      !Derivative.isSum("x*y"),
      !Derivative.isSum("2"),
      !Derivative.isSum("x"),
      !Derivative.isSum("")
    )

  test("Derivative.addend корректно выделяет слагаемые"):
    expect.all(
      Derivative.addend("2+x") == "2",
      Derivative.addend("x+2") == "x",
      Derivative.addend("x+y") == "x",
      Derivative.addend("2 + x") == "2",
      Derivative.addend("x +2 + 123") == "x",
      Derivative.addend("x+ y") == "x",
      Derivative.addend("2*x") == "",
      Derivative.addend("x*2") == "",
      Derivative.addend("x*y") == "",
      Derivative.addend("2") == "",
      Derivative.addend("x") == "",
      Derivative.addend("") == ""
    )

  test("Derivative.augend корректно выделяет уменьшаемые"):
    expect.all(
      Derivative.augend("2+x") == "x",
      Derivative.augend("x+2") == "2",
      Derivative.augend("x+y") == "y",
      Derivative.augend("2 + x") == "x",
      Derivative.augend("x +2 + 123") == "2 + 123",
      Derivative.augend("x+ y") == "y",
      Derivative.augend("2*x") == "",
      Derivative.augend("x*2") == "",
      Derivative.augend("x*y") == "",
      Derivative.augend("2") == "",
      Derivative.augend("x") == "",
      Derivative.augend("") == ""
    )

  test("Derivative.isProduct корректно идентифицирует произведения"):
    expect.all(
      Derivative.isProduct("2*x"),
      Derivative.isProduct("x*2"),
      Derivative.isProduct("x*y"),
      Derivative.isProduct("2 * x"),
      Derivative.isProduct("x *2"),
      Derivative.isProduct("x* y"),
      !Derivative.isProduct("2+x"),
      !Derivative.isProduct("x+2"),
      !Derivative.isProduct("x+y"),
      !Derivative.isProduct("2"),
      !Derivative.isProduct("x"),
      !Derivative.isProduct("")
    )

  test("Derivative.multiplier корректно выделяет множители"):
    expect.all(
      Derivative.multiplier("2*x") == "2",
      Derivative.multiplier("x*2") == "x",
      Derivative.multiplier("x*y") == "x",
      Derivative.multiplier("x*y*z") == "x",
      Derivative.multiplier("2 * x") == "2",
      Derivative.multiplier("x *2") == "x",
      Derivative.multiplier("x* y") == "x",
      Derivative.multiplier("2+x") == "",
      Derivative.multiplier("x+2") == "",
      Derivative.multiplier("x+y") == "",
      Derivative.multiplier("2") == "",
      Derivative.multiplier("x") == "",
      Derivative.multiplier("") == ""
    )

  test("Derivative.multiplicand корректно выделяет множимые"):
    expect.all(
      Derivative.multiplicand("2*x") == "x",
      Derivative.multiplicand("x*2") == "2",
      Derivative.multiplicand("x*y") == "y",
      Derivative.multiplicand("x*y*z") == "y*z",
      Derivative.multiplicand("2 * x") == "x",
      Derivative.multiplicand("x *2") == "2",
      Derivative.multiplicand("x* y") == "y",
      Derivative.multiplicand("2+x") == "",
      Derivative.multiplicand("x+2") == "",
      Derivative.multiplicand("x+y") == "",
      Derivative.multiplicand("2") == "",
      Derivative.multiplicand("x") == "",
      Derivative.multiplicand("") == ""
    )

  test("Derivative.makeSum корректно создает сумму"):
    expect.all(
      Derivative.makeSum(Number(0), x) == x,
      Derivative.makeSum(x, Number(0)) == x,
      Derivative.makeSum(Number(2), Number(3)) == Number(5),
      Derivative.makeSum(Number(2), x) == Complex(Add, Number(2), x),
      Derivative.makeSum(x, Number(2)) == Complex(Add, x, Number(2)),
      Derivative.makeSum(x, y) == Complex(Add, x, y)
    )

  test("Derivative.makeProduct корректно создает произведение"):
    expect.all(
      Derivative.makeProduct(Number(0), x) == Number(0),
      Derivative.makeProduct(x, Number(0)) == Number(0),
      Derivative.makeProduct(Number(2), Number(3)) == Number(6),
      Derivative.makeProduct(Number(1), x) == x,
      Derivative.makeProduct(x, Number(1)) == x,
      Derivative.makeProduct(Number(2), x) == Complex(Mul, Number(2), x),
      Derivative.makeProduct(x, Number(2)) == Complex(Mul, x, Number(2)),
      Derivative.makeProduct(x, y) == Complex(Mul, x, y)
    )

  test("Derivative.parseExpression корректно парсит выражения"):
    expect.all(
      Derivative.parseExpression("2") == Right(Number(2)),
      Derivative.parseExpression("123") == Right(Number(123)),
      Derivative.parseExpression("x") == Right(Variable("x")),
      Derivative.parseExpression("2 + 2") == Right(Number(4)),
      Derivative.parseExpression("2 + x") == Right(Complex(Add, Number(2), x)),
      Derivative.parseExpression("x + 2") == Right(Complex(Add, x, Number(2))),
      Derivative.parseExpression("x + y") == Right(Complex(Add, x, y)),
      Derivative.parseExpression("").isLeft,
      Derivative.parseExpression("xy").isLeft
    )

  test(
    "Derivative.parseExpression корректно парсит суммы из более чем двух слагаемых"
  ):
    expect.all(
      Derivative.parseExpression("2 + 2 + 3") == Right(Number(7)),
      Derivative.parseExpression("2 + x + 3") == Right(Complex(
        Add,
        Number(2),
        Complex(Add, x, Number(3))
      )),
      Derivative.parseExpression("x + 2 + y") == Right(Complex(
        Add,
        x,
        Complex(Add, Number(2), y)
      )),
      Derivative.parseExpression("x + y + z") == Right(Complex(
        Add,
        x,
        Complex(Add, y, Variable("z"))
      ))
    )

  test("Derivative.parseExpression корректно парсит произведения"):
    expect.all(
      Derivative.parseExpression("2*x") == Right(Complex(Mul, Number(2), x)),
      Derivative.parseExpression("x*2") == Right(Complex(Mul, x, Number(2))),
      Derivative.parseExpression("x*y") == Right(Complex(Mul, x, y)),
      Derivative.parseExpression("2 * x") == Right(Complex(Mul, Number(2), x)),
      Derivative.parseExpression("x *2") == Right(Complex(Mul, x, Number(2))),
      Derivative.parseExpression("x* y") == Right(Complex(Mul, x, y))
    )

  test(
    "Derivative.parseExpression корректно парсит произведения из более чем двух множителей"
  ):
    expect.all(
      Derivative.parseExpression("2*x*3") == Right(Complex(
        Mul,
        Number(2),
        Complex(Mul, x, Number(3))
      )),
      Derivative.parseExpression("2*x*y") == Right(Complex(
        Mul,
        Number(2),
        Complex(Mul, x, y)
      )),
      Derivative.parseExpression("x*2*y") == Right(Complex(
        Mul,
        x,
        Complex(Mul, Number(2), y)
      )),
      Derivative.parseExpression("x*y*z") == Right(Complex(
        Mul,
        x,
        Complex(Mul, y, Variable("z"))
      ))
    )

  test("Derivative.isSameVariable корректно идентифицирует переменные"):
    expect.all(
      Derivative.isSameVariable(x, x),
      Derivative.isSameVariable(y, y),
      !Derivative.isSameVariable(x, y),
      !Derivative.isSameVariable(y, x)
    )

  test("Derivative.deriv корректно вычисляет производную"):
    expect.all(
      Derivative.deriv(Number(2), x) == Number(0),
      Derivative.deriv(x, x) == Number(1),
      Derivative.deriv(y, x) == Number(0),
      Derivative.deriv(Complex(Add, Number(2), x), x) == Number(1),
      Derivative.deriv(Complex(Add, x, Number(2)), x) == Number(1),
      Derivative.deriv(Complex(Add, x, y), x) == Number(1),
      Derivative.deriv(Complex(Mul, Number(2), x), x) == Number(2),
      Derivative.deriv(Complex(Mul, x, Number(2)), x) == Number(2),
      Derivative.deriv(Complex(Mul, x, y), x) == y
    )

  test(
    "Derivative.deriv корректно вычисляет производную в более сложных случах"
  ):
    expect.all(
      Derivative.deriv(
        Derivative.parseExpression("5").getOrElse(Number(0)),
        x
      ).show == "0",
      Derivative.deriv(
        Derivative.parseExpression("3*x").getOrElse(Number(0)),
        x
      ).show == "3",
      Derivative.deriv(
        Derivative.parseExpression("x*x + 3*x").getOrElse(Number(0)),
        x
      ).show == "((x + x) + 3)",
      Derivative.deriv(
        Derivative.parseExpression("x*x*x").getOrElse(Number(0)),
        x
      ).show == "((x*x) + (x*(x + x)))"
    )
end DerivativeSuite
