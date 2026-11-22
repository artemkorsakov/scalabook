# Symbolic Differentiation

Let us build a procedure that performs symbolic differentiation of algebraic expressions.
This procedure takes as arguments an algebraic expression and a variable,
and returns the derivative of the expression with respect to that variable.
For example, if the arguments to the procedure are \\(ax^{2} + bx + c\\) and x,
the procedure should return 2ax + b.

## Differentiation program with abstract data

To simplify the problem, consider a simple symbolic differentiation program
that works with expressions built only using addition and multiplication operations with two arguments.
Any such expression can be differentiated by applying the following reduction rules:

- \\(\frac{\partial c}{\partial x} = 0\\) for a constant or a variable different from x
- \\(\frac{\partial x}{\partial x} = 1\\)
- \\(\frac{\partial (u + v)}{\partial x} = \frac{\partial u}{\partial x} + \frac{\partial v}{\partial x}\\)
- \\(\frac{\partial (uv)}{\partial x} = u(\frac{\partial v}{\partial x}) + v(\frac{\partial u}{\partial x})\\)

Note that the last two rules are essentially recursive.
That is, to obtain the derivative of a sum, we first need to obtain the derivatives of the summands and add them.
Each of them in turn may be an expression that needs to be broken down into components.
By breaking them into smaller and smaller parts, we eventually reach a stage
where all parts are either constants or variables, and their derivatives will be either 0 or 1.

## Composing expressions and derivatives

Define the expression and the operations performed on it:

```dotty
object Derivative:
  enum Operation:
    case Add, Mul

  enum Expression:
    case Number(value: Int)
    case Variable(name: String)
    case Complex(oper: Operation, left: Expression, right: Expression)
```

Assume that we already have procedures that implement the following selectors, constructors, and predicates:

- `isNumber(e: String)`: Is `e` a number?
- `isVariable(e: String)`: Is `e` a variable?
- `isSameVariable(v1: Variable, v2: Variable)`: Are `v1` and `v2` the same variable?
- `isSum(e: String)`: Is `e` a sum?
- `addend(e: String)`: First addend of the sum `e`.
- `augend(e: String)`: Second addend of the sum `e`.
- `isProduct(e: String)`: Is `e` a product?
- `multiplier(e: String)`: First factor of the product `e`.
- `multiplicand(e: String)`: Second factor of the product `e`.
- `makeSum(exp1: Expression, exp2: Expression)`: Constructs the sum of `exp1` and `exp2`.
- `makeProduct(exp1: Expression, exp2: Expression)`: Constructs the product of `exp1` and `exp2`.

Using these procedures, we can express the rules for obtaining an expression:

- Numeric expression: if the input string represents a number,
  it is converted to `Expression.Number` with the integer value obtained from the string.
- Variable expression: if the input string represents a variable,
  it is converted to `Expression.Variable` with the string value of the variable name.
- Complex expression with addition operation: if the input string represents a sum (e.g. "a + b"),
  it is converted to an `Expression.Complex` object with operation `Operation.Add` and two sub-expressions: the addend (left operand) and the augend (right operand).
- Complex expression with multiplication operation: if the input string represents a product (e.g. "a * b"),
  it is converted to an `Expression.Complex` object with operation `Operation.Mul` and two sub-expressions: the multiplier (left operand) and the multiplicand (right operand).
- If the input string does not match any of these patterns, the function returns `Left` with an error message indicating that the expression type is unknown.

```dotty
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
  else Left(s"unknown expression type -- DERIV: $exp")
```

The differentiation rules can be expressed as the following procedure:

- Numbers: the derivative of a number is `0`.
- Variables: if the variable matches the given one, the derivative is `1`, otherwise `0`.
- Addition: the derivative of a sum is the sum of the derivatives of the addends.
- Multiplication: the derivative of a product is the sum of the products of the derivative of one factor by the other factor and vice versa.

```dotty
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
```

The `deriv` procedure encapsulates the entire differentiation algorithm.
Since it is expressed in terms of abstract data, it will work
regardless of how we represent algebraic expressions.

## Representation of algebraic expressions

The data representation for the differentiation task will be as follows:

A number is a non-empty string containing only digits:

```dotty
def isNumber(e: String): Boolean =
  e.nonEmpty && e.forall(_.isDigit)
```

Variables are characters.

```dotty
def isVariable(e: String): Boolean =
  e.length == 1 && e.headOption.exists(_.isLetter)
```

A sum is a string containing "+":

```dotty
def isSum(e: String): Boolean = e.contains('+')
```

The first addend:

```dotty
def addend(e: String): String =
  e match
    case s"${f}+${s}" => f.trim
    case _            => ""
```

The second addend:

```dotty
def augend(e: String): String =
  e match
    case s"${f}+${s}" => s.trim
    case _            => ""
```

A product is a string containing "*":

```dotty
def isProduct(e: String): Boolean = e.contains('*')
```

The first factor of product `e`:

```dotty
def multiplier(e: String): String =
  e match
    case s"${f}*${s}" => f.trim
    case _            => ""
```

The second factor of product `e`:

```dotty
def multiplicand(e: String): String =
  e match
    case s"${f}*${s}" => s.trim
    case _            => ""
```

Two variables are the same if the characters representing them are equal:

```dotty
def isSameVariable(
    v1: Expression.Variable,
    v2: Expression.Variable
): Boolean =
  v1.name == v2.name
```

Constructing a sum and a product:

```dotty
def makeSum(exp1: Expression, exp2: Expression): Expression =
  Expression.Complex(Operation.Add, exp1, exp2)

def makeProduct(exp1: Expression, exp2: Expression): Expression =
  Expression.Complex(Operation.Mul, exp1, exp2)
```

Thus, we only need to connect this representation with the algorithm encapsulated in the `deriv` procedure,
and we get a working symbolic differentiation program.
Let us look at some examples of its behavior:

```dotty
println(
  s"Derivative of 5 is ${deriv(parseExpression("5").getOrElse(Number(0)), x).show}"
)
// Derivative of 5 is 0
println(
  s"Derivative of x is ${deriv(parseExpression("x").getOrElse(Number(0)), x).show}"
)
// Derivative of x is 1
println(
  s"Derivative of 2*x is ${deriv(parseExpression("2*x").getOrElse(Number(0)), x).show}"
)
// Derivative of 2*x is ((0*x) + (2*1))
println(
  s"Derivative of x+x is ${deriv(parseExpression("x+x").getOrElse(Number(0)), x).show}"
)
// Derivative of x+x is (1 + 1)
println(
  s"Derivative of x*y is ${deriv(parseExpression("x*y").getOrElse(Number(0)), x).show}"
)
// Derivative of x*y is ((1*y) + (x*0))
```

The answers produced by the program are correct; however, they need to be simplified.
It is true that \\(\frac{\partial (xy)}{\partial x} = 1 \times y + x \times 0\\), but we would like the program to know
that \\(x \times 0 = 0, 1 \times y = y\\), and \\(0 + y = y\\). The answer to the last example should simply be y.

## Expression simplification

Let us improve the `makeSum` procedure for creating a sum operation such that if both addends are numbers,
the procedure returns their sum. Additionally, if one of the addends is `0`, then `makeSum` returns the other addend.

```dotty
def makeSum(exp1: Expression, exp2: Expression): Expression =
  (exp1, exp2) match
    case (Number(0), exp2)        => exp2
    case (exp1, Number(0))        => exp1
    case (Number(n1), Number(n2)) => Number(n1 + n2)
    case _                        => Complex(Add, exp1, exp2)
```

Similarly, we will change `makeProduct` so as to embed the rules
that something multiplied by `0` is `0`, and multiplied by `1` is itself:

```dotty
def makeProduct(exp1: Expression, exp2: Expression): Expression =
  (exp1, exp2) match
    case (Number(0), _)           => Number(0)
    case (_, Number(0))           => Number(0)
    case (Number(1), exp2)        => exp2
    case (exp1, Number(1))        => exp1
    case (Number(n1), Number(n2)) => Number(n1 * n2)
    case _                        => Complex(Mul, exp1, exp2)
```

Here is how this version works on the same examples:

```dotty
println(
  s"Derivative of 5 is ${deriv(parseExpression("5").getOrElse(Number(0)), x).show}"
)
// Derivative of 5 is 0
println(
  s"Derivative of x is ${deriv(parseExpression("x").getOrElse(Number(0)), x).show}"
)
// Derivative of x is 1
println(
  s"Derivative of 2*x is ${deriv(parseExpression("2*x").getOrElse(Number(0)), x).show}"
)
// Derivative of 2*x is 2
println(
  s"Derivative of x+x is ${deriv(parseExpression("x+x").getOrElse(Number(0)), x).show}"
)
// Derivative of x+x is 2
println(
  s"Derivative of x*y is ${deriv(parseExpression("x*y").getOrElse(Number(0)), x).show}"
)
// Derivative of x*y is y
println(
  s"Derivative of x*x*x is ${deriv(parseExpression("x*x*x").getOrElse(Number(0)), x).show}"
)
// Derivative of x*x*x is ((x*x) + (x*(x + x)))
```

Although this is a noticeable improvement, the last example shows that much more needs to be done
before we get a program that reduces expressions to a form that can be considered "simplest" (in this case — \\(3x^{2}\\)).
The task of algebraic simplification is complex, among other things, because the form
that is simplest for one purpose may not be for another.

## Exponentiation

Using the differentiation rule for powers: \\(\frac{\partial u^{n}}{\partial x} = nu^{n - 1}\frac{\partial u}{\partial x}\\),
let us add one more operation — `Pow` (exponentiation):

```dotty
enum Operation:
  case ..., Pow
```

An exponentiation is a string containing the power operator `^`:

```dotty
def isPower(e: String): Boolean = e.contains('^')
```

The base and the exponent:

```dotty
def base(e: String): String =
  e match
    case s"${f}^${s}" => f.trim
    case _            => ""

def exponent(e: String): String =
  e match
    case s"${f}^${s}" => s.trim
    case _            => ""
```

Constructing the exponentiation operation:

- If the exponent is `0`, the method returns `Right(Number(1))` — any number raised to the power of `0` equals `1`.
- If the exponent is `1`, the method returns the base — any number raised to the power of `1` equals itself.
- If both the base and the exponent are numbers, the method returns `Right(Number(math.pow(b, n).toInt))`, which computes the exponentiation.
- If the base is any expression and the exponent is a number, the method returns `Right(Complex(Pow, b, n))`,
  which creates a new `Complex` expression with the `Pow` operator and two input expressions as operands.
- If none of the above cases match, the method returns `Left` with an error description.

```dotty
def makeExponentiation(
    exp1: Expression,
    exp2: Expression
): Either[String, Expression] =
  (exp1, exp2) match
    case (_, Number(0))         => Right(Number(1))
    case (b, Number(1))         => Right(b)
    case (Number(b), Number(n)) => Right(Number(math.pow(b, n).toInt))
    case (b, n: Number)         => Right(Complex(Pow, b, n))
    case _ => Left(s"Only a number is allowed as exponent: $exp2")
```

Parsing the exponentiation operation is similar to parsing other operations:

```dotty
def parseExpression(exp: String): Either[String, Expression] =
  ...
  else if isPower(exp) then
    for
      b              <- parseExpression(base(exp))
      e              <- parseExpression(exponent(exp))
      exponentiation <- makeExponentiation(b, e)
    yield exponentiation
  ...
```

Taking the derivative:

```dotty
def deriv(
    exp: Expression,
    variabe: Expression.Variable
): Either[String, Expression] =
  exp match
    ...
    case Complex(Pow, exp, Number(p)) =>
      for
        d <- deriv(exp, variabe)
        e <- makeExponentiation(exp, Number(p - 1))
      yield makeProduct(makeProduct(Number(p), e), d)
    case _ =>
      Left(s"Unknown derivative: $exp")
```

The result of taking the derivative of `"3*x^3 + 4*x^2 + 5*x + 6"`:

```
((3*(3*(x^{2}))) + ((4*(2*x)) + 5))
```

The result is correct but still requires simplification.

## Further simplification

Some results are absolutely correct but can be simplified. For example:

```text
Derivative of x*x*x is '((x*x) + (x*(x + x)))'
Derivative of 3*x^3 + 4*x^2 + 5*x + 6 is '((3*(3*(x^{2}))) + ((4*(2*x)) + 5))'
```

The following cases can be simplified:

- Sum of two identical expressions simplifies to multiplying `2` by the expression: `x + x = 2*x`
- Multiplying two numbers by an expression simplifies to multiplying the product by the expression: `4*(2*x) = 8*x`
- In a sequence of multiplications, numbers can be moved to the front: `x*2*y => 2*x*y`
- Multiplying two identical expressions simplifies to raising the expression to the second power: `x*x = x^2`
- Multiplying an expression by the same expression raised to a power simplifies to raising the expression to the power plus 1: `x*x^4 = x^5`

```dotty
def makeSum(exp1: Expression, exp2: Expression): Expression =
  (exp1, exp2) match
    ...
    case (exp1, exp2) if exp1 == exp2 => makeProduct(Number(2), exp1)
    case _                            => Complex(Add, exp1, exp2)

def makeProduct(exp1: Expression, exp2: Expression): Expression =
  (exp1, exp2) match
    ...
    case (Number(n1), Complex(Mul, Number(n2), exp)) =>
      makeProduct(Number(n1 * n2), exp)
    case (exp1, Complex(Mul, Number(n), exp2)) =>
      makeProduct(Number(n), makeProduct(exp1, exp2))
    case (x: Variable, y: Variable) if x == y =>
      Complex(Pow, x, Number(2))
    case (x: Variable, Complex(Pow, y: Variable, Number(n))) if x == y =>
      Complex(Pow, x, Number(n + 1))
    case _ => Complex(Mul, exp1, exp2)
```

The examples simplify to the following results:

```text
Derivative of x*x*x is '(3*(x^{2}))'
Derivative of 3*x^3 + 4*x^2 + 5*x + 6 is '((9*(x^{2})) + ((8*x) + 5))'
```

Further improvements could include adding support for "parentheses" and standard algebraic notation.

---

**References:**

- [Source code][sample]
- [Abelson Harold, Sussman Gerald Jay, Sussman Julie - Structure and Interpretation of Computer Programs][SICP]
- [Eli Bendersky's solution](https://eli.thegreenplace.net/tag/sicp)

[SICP]: https://web.mit.edu/6.001/6.037/sicp.pdf
[sample]: https://gitverse.ru/scalabook/algorithms
