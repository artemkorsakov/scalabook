object Derivative:
  enum Operation:
    case Add, Mul

  enum Expression:
    self =>

    case Number(value: Int)
    case Variable(name: String)
    case Complex(oper: Operation, left: Expression, right: Expression)

    def show: String =
      self match
        case Number(value)  => value.toString
        case Variable(name) => name
        case Complex(Operation.Add, left, right) =>
          s"(${left.show} + ${right.show})"
        case Complex(Operation.Mul, left, right) =>
          s"(${left.show}*${right.show})"

  import Expression.*
  import Operation.*

  // Является ли `e` числом?
  def isNumber(e: String): Boolean =
    e.nonEmpty && e.forall(_.isDigit)

  // Является ли `e` переменной?
  def isVariable(e: String): Boolean =
    e.length == 1 && e.headOption.exists(_.isLetter)

  // Является ли `e` суммой?
  def isSum(e: String): Boolean = e.contains('+')

  // Первое слагаемое суммы `e`.
  def addend(e: String): String =
    e match
      case s"${f}+${s}" => f.trim
      case _            => ""

  // Второе слагаемое суммы `e`.
  def augend(e: String): String =
    e match
      case s"${f}+${s}" => s.trim
      case _            => ""

  // Является ли `e` произведением?
  def isProduct(e: String): Boolean = e.contains('*')

  // Первый множитель произведения `e`.
  def multiplier(e: String): String =
    e match
      case s"${f}*${s}" => f.trim
      case _            => ""

  // Второй множитель произведения `e`.
  def multiplicand(e: String): String =
    e match
      case s"${f}*${s}" => s.trim
      case _            => ""

  def makeSum(exp1: Expression, exp2: Expression): Expression =
    (exp1, exp2) match
      case (Number(0), exp2)        => exp2
      case (exp1, Number(0))        => exp1
      case (Number(n1), Number(n2)) => Number(n1 + n2)
      case _                        => Complex(Add, exp1, exp2)

  def makeProduct(exp1: Expression, exp2: Expression): Expression =
    (exp1, exp2) match
      case (Number(0), _)           => Number(0)
      case (_, Number(0))           => Number(0)
      case (Number(1), exp2)        => exp2
      case (exp1, Number(1))        => exp1
      case (Number(n1), Number(n2)) => Number(n1 * n2)
      case _                        => Complex(Mul, exp1, exp2)

  def parseExpression(exp: String): Either[String, Expression] =
    if isNumber(exp) then Right(Expression.Number(exp.toInt))
    else if isVariable(exp) then Right(Expression.Variable(exp))
    else if isSum(exp) then
      for
        a1 <- parseExpression(addend(exp))
        a2 <- parseExpression(augend(exp))
      yield makeSum(a1, a2)
    else if isProduct(exp) then
      for
        m1 <- parseExpression(multiplier(exp))
        m2 <- parseExpression(multiplicand(exp))
      yield makeProduct(m1, m2)
    else Left(s"неизвестный тип выражения -- DERIV: $exp")

  // Являются ли `v1` и `v2` одной и той же переменной?
  def isSameVariable(
      v1: Expression.Variable,
      v2: Expression.Variable
  ): Boolean =
    v1.name == v2.name

  def deriv(exp: Expression, variabe: Expression.Variable): Expression =
    exp match
      case _: Expression.Number => Expression.Number(0)
      case v: Expression.Variable if isSameVariable(v, variabe) =>
        Expression.Number(1)
      case _: Expression.Variable => Expression.Number(0)
      case Expression.Complex(Operation.Add, left, right) =>
        makeSum(deriv(left, variabe), deriv(right, variabe))
      case Expression.Complex(Operation.Mul, left, right) =>
        makeSum(
          makeProduct(deriv(left, variabe), right),
          makeProduct(left, deriv(right, variabe))
        )

end Derivative

import Derivative.*
import Derivative.Expression.*

val x: Variable = Variable("x")

println(
  s"Производная от 5 равна ${deriv(parseExpression("5").getOrElse(Number(0)), x).show}"
)
println(
  s"Производная от x равна ${deriv(parseExpression("x").getOrElse(Number(0)), x).show}"
)
println(
  s"Производная от 2*x равна ${deriv(parseExpression("2*x").getOrElse(Number(0)), x).show}"
)
println(
  s"Производная от x+x равна ${deriv(parseExpression("x+x").getOrElse(Number(0)), x).show}"
)
println(
  s"Производная от x*y равна ${deriv(parseExpression("x*y").getOrElse(Number(0)), x).show}"
)
println(
  s"Производная от x*x*x равна ${deriv(parseExpression("x*x*x").getOrElse(Number(0)), x).show}"
)
