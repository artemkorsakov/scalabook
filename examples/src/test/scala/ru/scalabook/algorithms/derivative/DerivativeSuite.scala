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

  test("Derivative.isPower корректно идентифицирует степени"):
    expect.all(
      Derivative.isPower("x^2"),
      Derivative.isPower("2^3"),
      Derivative.isPower("x^y"),
      !Derivative.isPower("2+2"),
      !Derivative.isPower("x+2"),
      !Derivative.isPower("x*y"),
      !Derivative.isPower("2"),
      !Derivative.isPower("x"),
      !Derivative.isPower("")
    )

  test("Derivative.base корректно выделяет основание"):
    expect.all(
      Derivative.base("x^2") == "x",
      Derivative.base("2^3") == "2",
      Derivative.base("x^y") == "x",
      Derivative.base("2+2") == "",
      Derivative.base("x+2") == "",
      Derivative.base("x*y") == "",
      Derivative.base("2") == "",
      Derivative.base("x") == "",
      Derivative.base("") == ""
    )

  test("Derivative.exponent корректно выделяет показатель"):
    expect.all(
      Derivative.exponent("x^2") == "2",
      Derivative.exponent("2^3") == "3",
      Derivative.exponent("x^y") == "y",
      Derivative.exponent("2+2") == "",
      Derivative.exponent("x+2") == "",
      Derivative.exponent("x*y") == "",
      Derivative.exponent("2") == "",
      Derivative.exponent("x") == "",
      Derivative.exponent("") == ""
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

  test("Derivative.makeExponentiation корректно создает степень"):
    whenSuccess(Derivative.makeExponentiation(Number(2), Number(0))): expon =>
      expect.same(Number(1), expon)
    .and:
      whenSuccess(Derivative.makeExponentiation(x, Number(0))): expon =>
        expect.same(Number(1), expon)
    .and:
      whenSuccess(Derivative.makeExponentiation(Number(2), Number(1))): expon =>
        expect.same(Number(2), expon)
    .and:
      whenSuccess(Derivative.makeExponentiation(x, Number(1))): expon =>
        expect.same(x, expon)
    .and:
      whenSuccess(Derivative.makeExponentiation(Number(2), Number(3))): expon =>
        expect.same(Number(8), expon)
    .and:
      whenSuccess(Derivative.makeExponentiation(x, Number(2))): expon =>
        expect.same(Complex(Pow, x, Number(2)), expon)
    .and:
      expect.all(
        Derivative.makeExponentiation(Number(2), x).isLeft,
        Derivative.makeExponentiation(x, y).isLeft
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

  test("Derivative.parseExpression корректно парсит взятие степени"):
    expect.all(
      Derivative.parseExpression("x^2") == Right(Complex(Pow, x, Number(2))),
      Derivative.parseExpression("2^3") == Right(Number(8)),
      Derivative.parseExpression("x^y").isLeft,
      Derivative.parseExpression("2^2^3") == Right(Number(256)),
      Derivative.parseExpression("x^2^y").isLeft,
      Derivative.parseExpression("x^y^2").isLeft,
      Derivative.parseExpression("x^y^z").isLeft
    )

  test("Derivative.isSameVariable корректно идентифицирует переменные"):
    expect.all(
      Derivative.isSameVariable(x, x),
      Derivative.isSameVariable(y, y),
      !Derivative.isSameVariable(x, y),
      !Derivative.isSameVariable(y, x)
    )

  test("Derivative.deriv корректно вычисляет производную"):
    whenSuccess(Derivative.deriv(Number(2), x)): d =>
      expect.same(Number(0), d)
    .and:
      whenSuccess(Derivative.deriv(x, x)): d =>
        expect.same(Number(1), d)
    .and:
      whenSuccess(Derivative.deriv(y, x)): d =>
        expect.same(Number(0), d)
    .and:
      whenSuccess(Derivative.deriv(Complex(Add, Number(2), x), x)): d =>
        expect.same(Number(1), d)
    .and:
      whenSuccess(Derivative.deriv(Complex(Add, x, Number(2)), x)): d =>
        expect.same(Number(1), d)
    .and:
      whenSuccess(Derivative.deriv(Complex(Add, x, y), x)): d =>
        expect.same(Number(1), d)
    .and:
      whenSuccess(Derivative.deriv(Complex(Mul, Number(2), x), x)): d =>
        expect.same(Number(2), d)
    .and:
      whenSuccess(Derivative.deriv(Complex(Mul, x, Number(2)), x)): d =>
        expect.same(Number(2), d)
    .and:
      whenSuccess(Derivative.deriv(Complex(Mul, x, y), x)): d =>
        expect.same(y, d)
    .and:
      whenSuccess(Derivative.deriv(Complex(Pow, x, Number(2)), x)): d =>
        expect(Complex(Mul, Number(2), x) == d)
    .and:
      whenSuccess(Derivative.deriv(Complex(Pow, x, Number(200)), x)): d =>
        expect(Complex(Mul, Number(200), Complex(Pow, x, Number(199))) == d)

  test(
    "Derivative.deriv корректно вычисляет производную в более сложных случах"
  ):
    whenSuccess(Derivative.parseExpression("5")): exp =>
      whenSuccess(Derivative.deriv(exp, x)): d =>
        expect.same("0", d.show)
    .and:
      whenSuccess(Derivative.parseExpression("3*x")): exp =>
        whenSuccess(Derivative.deriv(exp, x)): d =>
          expect.same("3", d.show)
    .and:
      whenSuccess(Derivative.parseExpression("x*x + 3*x")): exp =>
        whenSuccess(Derivative.deriv(exp, x)): d =>
          expect.same("((x + x) + 3)", d.show)
    .and:
      whenSuccess(Derivative.parseExpression("x*x*x")): exp =>
        whenSuccess(Derivative.deriv(exp, x)): d =>
          expect.same("((x*x) + (x*(x + x)))", d.show)

  test("Derivative.deriv корректно вычисляет производную от степени"):
    whenSuccess(Derivative.parseExpression("x^2")): exp =>
      whenSuccess(Derivative.deriv(exp, x)): d =>
        expect.same("(2*x)", d.show)
    .and:
      whenSuccess(Derivative.parseExpression("x^3")): exp =>
        whenSuccess(Derivative.deriv(exp, x)): d =>
          expect.same("(3*(x^{2}))", d.show)
    .and:
      whenSuccess(Derivative.parseExpression("x^4")): exp =>
        whenSuccess(Derivative.deriv(exp, x)): d =>
          expect.same("(4*(x^{3}))", d.show)
end DerivativeSuite
