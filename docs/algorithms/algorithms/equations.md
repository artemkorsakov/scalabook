# Решение уравнений

## Квадратное уравнение

**Алгоритм:**

[Квадратное уравнение][square] — алгебраическое уравнение второй степени с общим видом
\\(ax^{2} + bx + c = 0\\), где \\(a \neq 0\\).

Решение квадратного уравнения можно получить по формуле:

- \\(x = \frac{-b \pm \sqrt{b^{2} - 4ac}}{2a}\\), если \\(D = b^{2} - 4ac \geq 0\\)
- \\(x = \frac{-b \pm \textit{i} \sqrt{4ac - b^{2}}}{2a}\\), если \\(D < 0\\)

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
final case class QuadraticEquation(a: Double, b: Double, c: Double):
  private val d = b * b - 4 * a * c
  
  def solutionsInIntegers: Array[Long] =
    if d < 0 || !d.isWhole || !math.sqrt(d).isWhole then Array.empty[Long]
    else
      val sqrt = math.round(math.sqrt(d))
      val den  = 2 * a
      if sqrt == 0 then
        if b % den == 0 then Array((-b / den).toLong) else Array.empty[Long]
      else
        val x1 = -b + sqrt
        val x2 = -b - sqrt
        (x1 % den == 0, x2 % den == 0) match
          case (true, true)   => Array((x1 / den).toLong, (x2 / den).toLong)
          case (true, false)  => Array((x1 / den).toLong)
          case (false, true)  => Array((x2 / den).toLong)
          case (false, false) => Array.empty[Long]

  def solutions: Array[Double] =
    if d < 0 then Array.empty[Double]
    else
      val sqrt = math.sqrt(d)
      val den  = 2 * a
      if sqrt == 0 then Array(-b / den)
      else Array((-b + sqrt) / den, (-b - sqrt) / den)

  def solutionsInComplexNumbers: Array[Complex[Double]] =
    if d >= 0 then solutions.map(Complex(_, 0.0))
    else
      val sqrt = math.sqrt(-d)
      val den  = 2 * a
      Array(
        Complex(-b / den, sqrt / den),
        Complex(-b / den, -sqrt / den)
      )
end QuadraticEquation

QuadraticEquation(1, -5, 6).solutionsInIntegers 
// Array(3, 2)
QuadraticEquation(3, -4, 1).solutions 
// Array(1.0, 0.3333333333333333)
QuadraticEquation(1, -2, 2).solutionsInComplexNumbers 
// Array((1.0 + 1.0i), (1.0 + -1.0i))
```

**Метрики:**

```dotty
@main def solutionsInIntegersBench(): Array[Long] =
  QuadraticEquation(1, -5, 6).solutionsInIntegers

// CPU Time: 216 ms
// Allocation memory size: 9,61 MB      
```

```dotty
@main def solutionsBench(): Array[Double] =
  QuadraticEquation(3, -4, 1).solutions

// CPU Time: 36 ms
// Allocation memory size: 1,73 MB      
```

```dotty
@main def solutionsInComplexNumbersBench(): Array[Complex[Double]] =
  QuadraticEquation(1, -2, 2).solutionsInComplexNumbers

// CPU Time: 48 ms
// Allocation memory size: 3,29 MB      
```

## Уравнение Пелля

**Алгоритм:**

[Метод «чакравала»][chakravala] для решения [уравнения Пелля][pell].

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

```dotty
final case class DiophantineEquation(d: Int):

  def minimalEquation: Option[(BigInt, BigInt)] =
    if math.sqrt(d.toDouble).isWhole then None
    else
      val a         = BigInt(math.round(math.sqrt(d.toDouble)))
      val k         = a.pow(2) - d
      var iteration = Iteration(a, BigInt(1), k, d)
      while !iteration.isSuccess
      do iteration = iteration.getNext
      Some((iteration.a, iteration.b))

  def isSuccess(x: BigInt, y: BigInt): Boolean =
    x.pow(2) == y.pow(2) * d + 1

end DiophantineEquation

final case class Iteration(a: BigInt, b: BigInt, k: BigInt, d: Int):
  lazy val isSuccess: Boolean = k == BigInt(1)

  def getNext: Iteration =
    if isSuccess then this
    else
      val m    = getM
      val newA = (a * m + b * d) / k.abs
      val newB = (a + b * m) / k.abs
      val newK = (m.pow(2) - d) / k
      Iteration(newA, newB, newK, d)

  private def getM: BigInt =
    var m    = BigInt(-1)
    var min  = d + 1
    val last = math.sqrt(2.0 * d).toInt
    for i <- 1 to last do
      if isMCorrect(BigInt(i)) then
        val tmp = math.abs(i * i - d)
        if tmp < min then
          min = tmp
          m = BigInt(i)

    if m == BigInt(-1) then
      m = BigInt(last + 1)
      while !isMCorrect(m)
      do m += 1

    m
  end getM

  private def isMCorrect(m: BigInt): Boolean =
    (a + b * m).abs % k.abs == BigInt(0)

end Iteration

DiophantineEquation(67).minimalEquation
// Some((48842,5967))
```

**Метрики:**

```dotty
@main def minimalDiophantineEquationBench(): Unit =
  println(DiophantineEquation(67).minimalEquation)

// CPU Time: 287 ms
// Allocation memory size: 11,00 MB      
```

## Многочлен

**Алгоритм:**

Определение коэффициентов многочлена (k-1)-й степени (\\(a\_{1}x^{k-1} + ... + a\_{i}x^{k-i} + ... + a\_{k}\\)) 
при заданных k решениях (\\(s\_{1}, ... , s\_{k}\\)) для x равного \\(1, 2, ..., k\\).

**Оценка:**

- **Время** - ???
- **Память** - ???

**Код:**

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

polynomialCoefficients(3, Array(1, 683, 44287)
// Array(21461, -63701, 42241)
// 21461 * 1 * 1 - 63701 * 1 + 42241 = 1
// 21461 * 2 * 2 - 63701 * 2 + 42241 = 683
// 21461 * 3 * 3 - 63701 * 3 + 42241 = 44287
```

**Метрики:**

```dotty
@main def polynomialCoefficientsBench(): Array[Long] =
  polynomialCoefficients(3, Array(1, 683, 44287))

// CPU Time: 312 ms
// Allocation memory size: 11,66 MB      
```

---

**Ссылки:**

- [Квадратное уравнение - Wiki][square]
- [Уравнение Пелля - Wiki][pell]
- [Метод «чакравала» - Wiki][chakravala]
- [Многочлен - Wiki][poly]


[square]: https://ru.wikipedia.org/wiki/%D0%9A%D0%B2%D0%B0%D0%B4%D1%80%D0%B0%D1%82%D0%BD%D0%BE%D0%B5_%D1%83%D1%80%D0%B0%D0%B2%D0%BD%D0%B5%D0%BD%D0%B8%D0%B5
[pell]: https://ru.wikipedia.org/wiki/%D0%A3%D1%80%D0%B0%D0%B2%D0%BD%D0%B5%D0%BD%D0%B8%D0%B5_%D0%9F%D0%B5%D0%BB%D0%BB%D1%8F
[chakravala]: https://ru.wikipedia.org/wiki/%D0%9C%D0%B5%D1%82%D0%BE%D0%B4_%C2%AB%D1%87%D0%B0%D0%BA%D1%80%D0%B0%D0%B2%D0%B0%D0%BB%D0%B0%C2%BB
[poly]: https://ru.wikipedia.org/wiki/%D0%9C%D0%BD%D0%BE%D0%B3%D0%BE%D1%87%D0%BB%D0%B5%D0%BD
