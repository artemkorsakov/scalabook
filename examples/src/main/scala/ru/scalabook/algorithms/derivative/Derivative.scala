package ru.scalabook.algorithms.derivative

object Derivative:
  enum Operation:
    case Add, Mul, Pow

  enum Expression:
    self =>

    case Number(value: Int)
    case Variable(name: String)
    case Complex(oper: Operation, left: Expression, right: Expression)

    def show: String =
      self match
        case Number(value)                       => value.toString
        case Variable(name)                      => name
        case Complex(Operation.Add, left, right) =>
          s"(${left.show} + ${right.show})"
        case Complex(Operation.Mul, left, right) =>
          s"(${left.show}*${right.show})"
        case Complex(Operation.Pow, left, right) =>
          s"(${left.show}^{${right.show}})"

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

  // Является ли `e` степенью?
  def isPower(e: String): Boolean = e.contains('^')

  // Основание степени `e`.
  def base(e: String): String =
    e match
      case s"${f}^${s}" => f.trim
      case _            => ""

  // Степень числа `e`.
  def exponent(e: String): String =
    e match
      case s"${f}^${s}" => s.trim
      case _            => ""

  def makeSum(exp1: Expression, exp2: Expression): Expression =
    (exp1, exp2) match
      case (Number(0), exp2)            => exp2
      case (exp1, Number(0))            => exp1
      case (Number(n1), Number(n2))     => Number(n1 + n2)
      case (exp1, exp2) if exp1 == exp2 => makeProduct(Number(2), exp1)
      case _                            => Complex(Add, exp1, exp2)

  def makeProduct(exp1: Expression, exp2: Expression): Expression =
    (exp1, exp2) match
      case (Number(0), _)                              => Number(0)
      case (_, Number(0))                              => Number(0)
      case (Number(1), exp2)                           => exp2
      case (exp1, Number(1))                           => exp1
      case (Number(n1), Number(n2))                    => Number(n1 * n2)
      case (Number(n1), Complex(Mul, Number(n2), exp)) =>
        makeProduct(Number(n1 * n2), exp)
      case (exp1, Complex(Mul, Number(n), exp2)) =>
        makeProduct(Number(n), makeProduct(exp1, exp2))
      case (x: Variable, y: Variable) if x == y =>
        Complex(Pow, x, Number(2))
      case (x: Variable, Complex(Pow, y: Variable, Number(n))) if x == y =>
        Complex(Pow, x, Number(n + 1))
      case _ => Complex(Mul, exp1, exp2)

  def makeExponentiation(
      exp1: Expression,
      exp2: Expression
  ): Either[String, Expression] =
    (exp1, exp2) match
      case (_, Number(0))         => Right(Number(1))
      case (b, Number(1))         => Right(b)
      case (Number(b), Number(n)) => Right(Number(math.pow(b, n).toInt))
      case (b, n: Number)         => Right(Complex(Pow, b, n))
      case _ => Left(s"В качестве степени допустимо только число: $exp2")

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
    else if isPower(exp) then
      for
        b              <- parseExpression(base(exp))
        e              <- parseExpression(exponent(exp))
        exponentiation <- makeExponentiation(b, e)
      yield exponentiation
    else Left(s"неизвестный тип выражения -- DERIV: $exp")

  // Являются ли `v1` и `v2` одной и той же переменной?
  def isSameVariable(
      v1: Expression.Variable,
      v2: Expression.Variable
  ): Boolean =
    v1.name == v2.name

  def deriv(
      exp: Expression,
      variabe: Expression.Variable
  ): Either[String, Expression] =
    exp match
      case _: Number                                 => Right(Number(0))
      case v: Variable if isSameVariable(v, variabe) =>
        Right(Number(1))
      case _: Variable               => Right(Number(0))
      case Complex(Add, left, right) =>
        for
          s1 <- deriv(left, variabe)
          s2 <- deriv(right, variabe)
        yield makeSum(s1, s2)
      case Complex(Mul, left, right) =>
        for
          d1 <- deriv(left, variabe)
          d2 <- deriv(right, variabe)
        yield makeSum(
          makeProduct(d1, right),
          makeProduct(left, d2)
        )
      case Complex(Pow, exp, Number(p)) =>
        for
          d <- deriv(exp, variabe)
          e <- makeExponentiation(exp, Number(p - 1))
        yield makeProduct(makeProduct(Number(p), e), d)
      case _ =>
        Left(s"Неизвестная производная: $exp")

end Derivative
