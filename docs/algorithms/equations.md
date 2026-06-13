{%
showPrev = true
prevPage = ackermann.html
showNext = true
nextPage = geometry.html
%}

# Решение уравнений

## Квадратное уравнение

**Алгоритм:**

[Квадратное уравнение][square] — алгебраическое уравнение второй степени с общим видом
\\(ax^{2} + bx + c = 0\\), где \\(a \neq 0\\).

Решение квадратного уравнения можно получить по формуле:

- \\(x = \frac{-b \pm \sqrt{b^{2} - 4ac}}{2a}\\), если \\(D = b^{2} - 4ac \geq 0\\)

Решение в комплексных числах:

- \\(x = \frac{-b \pm \textit{i} \sqrt{4ac - b^{2}}}{2a}\\), если \\(D < 0\\)

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

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

## Уравнение Пелля

**Алгоритм:**

[Метод «чакравала»][chakravala] для решения [уравнения Пелля][pell].

Рассмотрим переход к следующей итерации согласно методу «чакравала»:

- Итерация задается четверкой (a, b, k, d) такой, что \\(a^2 - d \times b^2 = k\\)
- Итерация считается успешной, если k равно 1 - решение уравнения Пелля найдено
- Для получения следующей итерации используется подстановка:
  \\(a \leftarrow {\frac {am+db}{|k|}}, b \leftarrow {\frac {a+bm}{|k|}}, k \leftarrow {\frac {m^{2}-d}{k}}\\)
- m подбирается следующим образом:
    - m считается корректным, если a + b * m кратно |k|
    - положительное m подбирается таким, чтобы \\(m^2 - d\\) было минимальным по модулю
    - вначале проверяем все "корректные" m от 1 до \\(\sqrt{2d}\\) 
    - если таких не найдено, то начиная с \\(\sqrt{2d} + 1\\) ищем первое "корректное" m

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

Для получения минимального решения уравнения Пелля:

- проверяем, что d - не является полным квадратом
- задаем первую итерацию:
    - а - ближайшее целое к корню из d
    - \\(k = a^2 - d\\)
    - b = 1
- ищем первую успешную (k = 1) итерацию

**Код:**

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

## Многочлен

**Алгоритм:**

Определение коэффициентов многочлена (k-1)-й степени (\\(a\_{1}x^{k-1} + ... + a\_{i}x^{k-i} + ... + a\_{k}\\)) 
при заданных k решениях (\\(s\_{1}, ... , s\_{k}\\)) для x равного \\(1, 2, ..., k\\).

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

polynomialCoefficients(3, Array(1, 683, 44287))
// Array(21461, -63701, 42241)
// 21461 * 1 * 1 - 63701 * 1 + 42241 = 1
// 21461 * 2 * 2 - 63701 * 2 + 42241 = 683
// 21461 * 3 * 3 - 63701 * 3 + 42241 = 44287
```

---

**Ссылки:**

- [Исходный код][sample]
- [Квадратное уравнение - Wiki][square]
- [Уравнение Пелля - Wiki][pell]
- [Метод «чакравала» - Wiki][chakravala]
- [Многочлен - Wiki][poly]


[square]: https://ru.wikipedia.org/wiki/%D0%9A%D0%B2%D0%B0%D0%B4%D1%80%D0%B0%D1%82%D0%BD%D0%BE%D0%B5_%D1%83%D1%80%D0%B0%D0%B2%D0%BD%D0%B5%D0%BD%D0%B8%D0%B5
[pell]: https://ru.wikipedia.org/wiki/%D0%A3%D1%80%D0%B0%D0%B2%D0%BD%D0%B5%D0%BD%D0%B8%D0%B5_%D0%9F%D0%B5%D0%BB%D0%BB%D1%8F
[chakravala]: https://ru.wikipedia.org/wiki/%D0%9C%D0%B5%D1%82%D0%BE%D0%B4_%C2%AB%D1%87%D0%B0%D0%BA%D1%80%D0%B0%D0%B2%D0%B0%D0%BB%D0%B0%C2%BB
[poly]: https://ru.wikipedia.org/wiki/%D0%9C%D0%BD%D0%BE%D0%B3%D0%BE%D1%87%D0%BB%D0%B5%D0%BD
[sample]: https://gitverse.ru/scalabook/algorithms
