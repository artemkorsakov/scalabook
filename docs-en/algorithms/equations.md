# Equation Solving

## Quadratic Equation

**Algorithm:**

[A quadratic equation][square] is a second-degree algebraic equation with the general form
\\(ax^{2} + bx + c = 0\\), where \\(a \neq 0\\).

The solution of a quadratic equation can be obtained by the formula:

- \\(x = \frac{-b \pm \sqrt{b^{2} - 4ac}}{2a}\\), if \\(D = b^{2} - 4ac \geq 0\\)

Solution in complex numbers:

- \\(x = \frac{-b \pm \textit{i} \sqrt{4ac - b^{2}}}{2a}\\), if \\(D < 0\\)

**Complexity:**

- **Time** - O(1)
- **Memory** - O(1)

**Code:**

```dotty
final case class Complex(real: Double, imag: Double)

final case class QuadraticEquation(a: Double, b: Double, c: Double):
  private val d = b * b - 4 * a * c

  def solutionsInIntegers: List[Long] =
    if d < 0 || !d.isWhole || !math.sqrt(d).isWhole then Nil
    else
      val sqrt = math.round(math.sqrt(d))
      val den  = 2 * a
      if sqrt == 0 then
        if b % den == 0 then List((-b / den).toLong) else Nil
      else
        val x1 = -b + sqrt
        val x2 = -b - sqrt
        (x1 % den == 0, x2 % den == 0) match
          case (true, true)   => List((x1 / den).toLong, (x2 / den).toLong)
          case (true, false)  => List((x1 / den).toLong)
          case (false, true)  => List((x2 / den).toLong)
          case (false, false) => Nil

  def solutions: List[Double] =
    if d < 0 then Nil
    else
      val sqrt = math.sqrt(d)
      val den  = 2 * a
      if sqrt == 0 then List(-b / den)
      else List((-b + sqrt) / den, (-b - sqrt) / den)

  def solutionsInComplexNumbers: List[Complex] =
    if d >= 0 then solutions.map(Complex(_, 0.0))
    else
      val sqrt = math.sqrt(-d)
      val den  = 2 * a
      List(
        Complex(-b / den, sqrt / den),
        Complex(-b / den, -sqrt / den)
      )
end QuadraticEquation

println(QuadraticEquation(1, -5, 6).solutionsInIntegers)
// List(3, 2)
println(QuadraticEquation(3, -4, 1).solutions)
// List(1.0, 0.3333333333333333)
println(QuadraticEquation(1, -2, 2).solutionsInComplexNumbers)
// List(Complex(1.0,1.0), Complex(1.0,-1.0))
```

## Pell's Equation

**Algorithm:**

The [chakravala method][chakravala] for solving [Pell's equation][pell].

Let us consider the transition to the next iteration according to the chakravala method:

- The iteration is given by a quadruple (a, b, k, d) such that \\(a^2 - d \times b^2 = k\\)
- An iteration is considered successful if k equals 1 — the solution to Pell's equation is found
- The following substitution is used to obtain the next iteration:
  \\(a \leftarrow {\frac {am+db}{|k|}}, b \leftarrow {\frac {a+bm}{|k|}}, k \leftarrow {\frac {m^{2}-d}{k}}\\)
- m is selected as follows:
    - m is considered correct if a + b * m is divisible by |k|
    - a positive m is selected such that \\(m^2 - d\\) is minimal in absolute value
    - first, check all "correct" m from 1 to \\(\sqrt{2d}\\)
    - if none are found, starting from \\(\sqrt{2d} + 1\\) search for the first "correct" m

```dotty
final case class Iteration(a: BigInt, b: BigInt, k: BigInt, d: Int):
  lazy val isSuccess: Boolean = k == BigInt(1)

  private lazy val getM: BigInt =
    val last = BigInt(math.sqrt(2.0 * d).toInt)
    val maybeM =
      (BigInt(1) to last)
        .filter(isMCorrect)
        .minByOption(i => (i * i - d).abs)

    @annotation.tailrec
    def loop(m: BigInt): BigInt =
      if isMCorrect(m) then m else loop(m + 1)

    maybeM.getOrElse(loop(last + 1))
  end getM

  def getNext: Iteration =
    if isSuccess then this
    else
      val m    = getM
      val newA = (a * m + b * d) / k.abs
      val newB = (a + b * m) / k.abs
      val newK = (m * m - d) / k
      Iteration(newA, newB, newK, d)

  private def isMCorrect(m: BigInt): Boolean =
    (a + b * m) % k.abs == BigInt(0)

end Iteration
```

To obtain the minimal solution to Pell's equation:

- check that d is not a perfect square
- set the first iteration:
    - a — the nearest integer to the square root of d
    - \\(k = a^2 - d\\)
    - b = 1
- search for the first successful (k = 1) iteration

**Code:**

```dotty
final case class DiophantineEquation(d: Int):

  def minimalEquation: Option[(BigInt, BigInt)] =
    val sd = math.sqrt(d.toDouble)
    if sd.isWhole then None
    else
      val a = BigInt(math.round(sd))
      val k = a.pow(2) - d

      @annotation.tailrec
      def loop(iteration: Iteration): Option[(BigInt, BigInt)] =
        if iteration.isSuccess then Some((iteration.a, iteration.b))
        else loop(iteration.getNext)

      loop(Iteration(a, BigInt(1), k, d))

DiophantineEquation(67).minimalEquation
// Some((48842,5967))
```

## Polynomial

**Algorithm:**

Determination of the coefficients of a polynomial of degree (k-1) (\\(a\_{1}x^{k-1} + ... + a\_{i}x^{k-i} + ... + a\_{k}\\))
given k solutions (\\(s\_{1}, ... , s\_{k}\\)) for x equal to \\(1, 2, ..., k\\).

**Code:**

```dotty
def polynomialCoefficients(k: Int, solutions: Array[Long]): Array[Long] =
  val aList = new Array[Long](k)

  val coefficientsList = coefficientsForSolutionSearching(k)
  for i <- k - 1 to 0 by -1 do
    val coefficients = coefficientsList(i)
    val index        = k - coefficients.length
    var divisor      = 0L
    for j <- coefficients.indices do
      aList(index) += coefficients(j) * solutions(coefficients.length - 1 - j)
      (0 until index).foreach: m =>
        aList(index) -= aList(m) * coefficients(j) * math
          .pow(coefficients.length.toDouble - j, k - 1.0 - m)
          .toLong
      divisor += coefficients(j) * math
        .pow(
          coefficients.length.toDouble - j,
          coefficients.length.toDouble - 1
        )
        .toLong
    aList(index) = aList(index) / divisor

  aList
end polynomialCoefficients

private def coefficientsForSolutionSearching(
    k: Int
): Vector[Vector[Long]] =
  if k == 1 then Vector(Vector(1L))
  else
    val coefficients = coefficientsForSolutionSearching(k - 1)
    val last         = coefficients.last
    val current = 1L +:
      (0 until last.length - 1).map(i => last(i + 1) - last(i)).toVector :+
      -last.last
    coefficients :+ current

polynomialCoefficients(3, Array(1, 683, 44287))
// Array(21461, -63701, 42241)
// 21461 * 1 * 1 - 63701 * 1 + 42241 = 1
// 21461 * 2 * 2 - 63701 * 2 + 42241 = 683
// 21461 * 3 * 3 - 63701 * 3 + 42241 = 44287
```

---

**References:**

- [Source code][sample]
- [Quadratic Equation - Wiki][square]
- [Pell's Equation - Wiki][pell]
- [Chakravala method - Wiki][chakravala]
- [Polynomial - Wiki][poly]


[square]: https://en.wikipedia.org/wiki/Quadratic_equation
[pell]: https://en.wikipedia.org/wiki/Pell%27s_equation
[chakravala]: https://en.wikipedia.org/wiki/Chakravala_method
[poly]: https://en.wikipedia.org/wiki/Polynomial
[sample]: https://gitverse.ru/scalabook/algorithms
