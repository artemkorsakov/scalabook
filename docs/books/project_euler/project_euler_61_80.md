# Задачи №61-№80

## Задача №61

[Задача №61 - Cyclical Figurate Numbers](https://projecteuler.net/problem=61)

> К фигурным (многоугольным) числам относятся треугольные, квадратные, пятиугольные, шестиугольные, семиугольные 
> и восьмиугольные числа, которые расчитываются по следующим формулам:
>
> Треугольные: \\(P\_{3,n}=n(n+1)/2 \rightarrow 1, 3, 6, 10, 15, \dots \\)
>
> Квадратные: \\(P\_{4,n}=n^{2} \rightarrow 1, 4, 9, 16, 25, \dots \\)
>
> Пятиугольные: \\(P\_{5,n}=n(3n-1)/2 \rightarrow 1, 5, 12, 22, 35, \dots \\)
>
> Шестиугольные: \\(P\_{6,n}=n(2n-1) \rightarrow 1, 6, 15, 28, 45, \dots \\)
>
> Семиугольные: \\(P\_{7,n}=n(5n-3)/2 \rightarrow 1, 7, 18, 34, 55, \dots \\)
>
> Восьмиугольные: \\(P\_{8,n}=n(3n-2) \rightarrow 1, 8, 21, 40, 65, \dots \\)
>
> Упорядоченное множество из трех четырехзначных чисел: 8128, 2882, 8281, обладает тремя интересными свойствами
> 
> - Множество является цикличным: последние две цифры каждого числа являются первыми двумя цифрами следующего 
>   (включая последнее и первое числа).
> - Каждый тип многоугольника — треугольник (P3,127=8128), квадрат (P4,91=8281) и пятиугольник (P5,44=2882) — 
>   представлены различными числами данного множества.
> - Это — единственное множество четырехзначных чисел, обладающее указанными свойствами.
> 
> Найдите сумму элементов единственного упорядоченного множества из шести цикличных четырехзначных чисел, 
> в котором каждый тип многоугольников — треугольник, квадрат, пятиугольник, шестиугольник, семиугольник 
> и восьмиугольник — представлены различными числами этого множества.

**Алгоритм:**

- В первую очередь сформируем списки всех четырехзначных циклических чисел.

**Код:**

```dotty
def triangleNumber(n: Int): Int = n * (n + 1) / 2

lazy val triangle = LazyList
  .from(1)
  .map(triangleNumber)
  .takeWhile(_ < 10000)
  .filter(_ >= 1000)
  .toList

def squareNumber(n: Int): Int = n * n
lazy val square = ... // аналогично triangle

def pentagonalNumber(n: Int): Int = n * (3 * n - 1) / 2
lazy val pentagonal =  ... // аналогично triangle

def hexagonalNumber(n: Int): Int = n * (2 * n - 1)
lazy val hexagonal =  ... // аналогично triangle

def heptagonalNumber(n: Int): Int = n * (5 * n - 3) / 2
lazy val heptagonal =  ... // аналогично triangle

def octagonalNumber(n: Int): Int = n * (3 * n - 2)
lazy val octagonal = ... // аналогично triangle
```

- Затем определим функцию, которая по заданным шести спискам чисел находит все циклические цепочки чисел.

**Код:**

```dotty
def getChain(
    list1: List[Int],
    list2: List[Int],
    list3: List[Int],
    list4: List[Int],
    list5: List[Int],
    list6: List[Int]
): List[List[Int]] =
  for
    first  <- list1
    second <- list2
    if first % 100 == second / 100
    third <- list3
    if second % 100 == third / 100
    fourth <- list4
    if third % 100 == fourth / 100
    fifth <- list5
    if fourth % 100 == fifth / 100
    sixth = (fifth % 100) * 100 + first / 100
    if list6.contains(sixth)
  yield List(first, second, third, fourth, fifth, sixth)
```

- Т.к. исходный набор циклический, то мы можем выбрать, с каких чисел начинать цикл.
   Т.о. можно начать с чисел `octagonal`, т.к. этот набор самый маленький.
   Остальные пять наборов могут располагаться в любом порядке.
   Используем генератор всевозможных перестановок 5 элементов.

**Код:**

```dotty
def xvariations[A](l: List[A], n: Int): List[List[A]] =
  def mixmany(x: A, ll: List[List[A]]): List[List[A]] =
    ll match
      case hd :: tl => foldone(x, hd) ::: mixmany(x, tl)
      case _        => Nil

  def foldone(x: A, ll: List[A]): List[List[A]] =
    (1 to ll.length).foldLeft(List(x :: ll))((a, i) =>
      mixone(i, x, ll) :: a
    )

  def mixone(i: Int, x: A, ll: List[A]): List[A] =
    ll.slice(0, i) ::: (x :: ll.slice(i, ll.length))

  if n > l.size then Nil
  else
    l match
      case _ :: _ if n == 1 => l.map(List(_))
      case hd :: tl =>
        mixmany(hd, xvariations(tl, n - 1)) ::: xvariations(tl, n)
      case _ => Nil
end xvariations

val permutations = xvariations(List(triangle, square, pentagonal, hexagonal, heptagonal), 5)
```

- Осталось перебрать все варианты.

**Код:**

```dotty
val chains =
  permutations.flatMap:
    case List(list1, list2, list3, list4, list5) =>
      getChain(octagonal, list1, list2, list3, list4, list5)
    case _ => List.empty

// List(List(1281, 8128, 2882, 8256, 5625, 2512))
```

## Задача №62

[Задача №62 - Cubic Permutations](https://projecteuler.net/problem=62)

> Можно найти перестановки куба \\(41063625 (345^{3})\\), чтобы получить еще два куба: 
> \\(56623104 (384^{3})\\) и \\(66430125 (405^{3})\\). 
> К слову, 41063625 является наименьшим кубом, для которого ровно три перестановки также являются кубами.
>
> Найдите наименьший куб, для которого ровно пять перестановок также являются кубами.

**Алгоритм:**

Простой перебор дает решение примерно за 50 секунд.

**Код:**

```dotty
val allCubes = (1L to 10000L).map(i => i * i * i)
val n        = allCubes.length

def isPermutation(a: Long, b: Long): Boolean =
  a.toString.sorted == b.toString.sorted

val count =
  allCubes.indices.find: i =>
    (i + 1 until n).count(j => isPermutation(allCubes(i), allCubes(j))) == 4
```

## Задача №63

[Задача №63 - Powerful Digit Counts](https://projecteuler.net/problem=63)

> Пятизначное число \\(16807=7^5\\) является также пятой степенью натурального числа. 
> Аналогично, девятизначное число \\(134217728=8^9\\) является девятой степенью.
>
> Сколько существует n-значных натуральных чисел, являющихся также и n-ми степенями натуральных чисел?

**Алгоритм:**

- Тривиальный случай: \\(a^1 = 1\\), где \\(1 \leq a \leq 9\\)
- Основанием n-й степени может быть только цифра от 2 до 9 (за исключением тривиального случая), 
  т.к. при \\(a \geq 10, a^{n}\\) содержит как минимум n + 1 цифр.
- Если \\(a^{k}\\) состоит из k цифр, и \\(a^{k + 1}\\) состоит из k цифр, то все последующие
  \\(a^{m}, m > k\\) состоят из менее чем m цифр, т.к. a < 10 и не может добавить более 1 цифры при увеличении степени.
- Получается, что необходимо проверить только такие \\(a^{k}\\), где \\(2 \leq a \leq 9\\), \\(1 \leq k \leq m\\),
  m - первое такое число, что \\(a^{m}\\) содержит менее m цифр.

**Код:**

```dotty
def countGoodNumber(a: Int): Int =
  LazyList
    .from(1)
    .takeWhile { n => BigInt(a).pow(n).toString.length == n }
    .length

val count = 1 + (2 to 9).foldLeft(0)(_ + countGoodNumber(_))
```

## Задача №64

[Задача №64 - Odd Period Square Roots](https://projecteuler.net/problem=64)

> Любой квадратный корень является периодическим, если записать его в виде непрерывных дробей в следующей форме:
>
> \\(\sqrt{N} = a\_0 + \frac{1}{a\_1 + \frac{1}{a\_2 + \frac{1}{a\_3 + \dots}}}\\)
>
> К примеру, рассмотрим \\(\sqrt{23}:\\)
> 
> \\(\sqrt{23} = 4 + \sqrt{23} - 4 = 4 + \frac{1}{\frac{1}{\sqrt{23} - 4}} = 4 + \frac{1}{1 + \frac{\sqrt{23} - 3}{7}}\\)
>
> Продолжив это преобразование, мы получим следующее приближение:
>
> \\(\sqrt{23} = 4 + \frac{1}{1 + \frac{1}{3 + \frac{1}{1 + \frac{1}{8 + \dots}}}}\\)
>
> Этот процесс можно обобщить в следующем виде:
>
> - \\(a\_0 = 4, \frac{1}{\sqrt{23} - 4} = \frac {\sqrt{23} + 4}{7} = 1 + \frac {\sqrt{23} - 3}{7}\\)
> - \\(a\_1 = 1, \frac{7}{\sqrt{23} - 3} = \frac {7(\sqrt{23} + 3)}{14} = 3 + \frac{\sqrt{23} - 3}{2}\\)
> - \\(a\_2 = 3, \frac{2}{\sqrt{23} - 3} = \frac {2(\sqrt{23} + 3)}{14} = 1 + \frac{\sqrt{23} - 4}{7}\\)
> - \\(a\_3 = 1, \frac{7}{\sqrt{23} - 4} = \frac {7(\sqrt{23} + 4)}{7} = 8 + \sqrt{23} - 4\\)
> - \\(a\_4 = 8, \frac{1}{\sqrt{23} - 4} = \frac {\sqrt{23} + 4}{7} = 1 + \frac{\sqrt{23} - 3}{7}\\)
> - \\(a\_5 = 1, \frac{7}{\sqrt{23} - 3} = \frac {7 (\sqrt{23} + 3)}{14} = 3 + \frac{\sqrt{23}-3}{2}\\)
> - \\(a\_6 = 3, \frac{2}{\sqrt{23} - 3} = \frac {2(\sqrt{23} + 3)}{14} = 1 + \frac {\sqrt{23}-4}{7}\\)
> - \\(a\_7 = 1, \frac{7}{\sqrt{23} - 4} = \frac {7(\sqrt{23} + 4)}{7} = 8 + \sqrt{23} - 4\\)
>
> Нетрудно заметить, что последовательность является периодической. 
> Для краткости введем обозначение \\(\sqrt{23}=\[4;(1,3,1,8)\]\\), 
> чтобы показать что блок (1,3,1,8) бесконечно повторяется.
>
> Первые десять представлений непрерывных дробей (иррациональных) квадратных корней:
>
> - \\(\sqrt{2} = \[1;(2)\]\\), период = 1
> - \\(\sqrt{3} = \[1;(1,2)\]\\), период = 2
> - \\(\sqrt{5} = \[2;(4)\]\\), период = 1
> - \\(\sqrt{6} = \[2;(2,4)\]\\), период = 2
> - \\(\sqrt{7} = \[2;(1,1,1,4)\]\\), период = 4
> - \\(\sqrt{8} = \[2;(1,4)\]\\), период = 2
> - \\(\sqrt{10} = \[3;(6)\]\\), период = 1
> - \\(\sqrt{11} = \[3;(3,6)\]\\), период = 2
> - \\(\sqrt{12} = \[3;(2,6)\]\\), период = 2
> - \\(\sqrt{13} = \[3;(1,1,1,1,6)\]\\), период = 5
> 
> Период является нечетным у ровно четырех непрерывных дробей при \\(N \le 13\\).
> 
> У скольких непрерывных дробей период является нечетным при \\(N \le 10000\\)?

**Алгоритм:**

Определим дробь как \\(\frac {irr \times \sqrt{n} + rat}{den}\\).
Какая будет следующая итерация дроби? 

\\(\frac {irr \times \sqrt{n} + rat}{den} = whole + \frac {irr \times \sqrt{n} + (rat - whole * den)}{den} = whole + \frac {1}{\frac {den}{irr \times \sqrt{n} + (rat - whole * den)}}\\),
где whole - целая часть исходной дроби.

Определим \\(k = whole * den - rat\\). Следующая итерация - это:

\\(\frac {den}{irr \times \sqrt{n} - k} = \frac {den (irr \times \sqrt{n} + k)}{irr^2 \times n - k^2} = \frac {den \times irr \times \sqrt{n} + den \times k}{irr^2 \times n - k^2}\\)

Определим d как наибольший общий делитель трех чисел: \\(den \times irr, den \times k, irr^2 \times n - k^2\\).
Тогда получим следующую трансформацию дроби:

- \\(irr \Rightarrow (den \times irr)/d \\)
- \\(rat \Rightarrow (den \times k)/d \\)
- \\(den \Rightarrow (irr^2 \times n - k^2)/d \\)

**Код:**

```dotty
@tailrec
def gcd(a: Int, b: Int): Int =
  if b == 0 then a else gcd(b, a % b)

case class Fraction(n: Int, irr: Int, rat: Int, den: Int):
  lazy val nextFraction: Fraction =
    val whole   = ((irr * math.sqrt(n) + rat) / den).toInt
    val k       = whole * den - rat
    val nextIrr = irr * den
    val nextRat = den * k
    val nextDen = n * irr * irr - k * k
    val gcdNum  = gcd(gcd(nextIrr, nextRat), nextDen)
    Fraction(n, nextIrr / gcdNum, nextRat / gcdNum, nextDen / gcdNum)

Fraction(23, 1, 0, 1).nextFraction // (1, 4, 7)
Fraction(23, 1, 4, 7).nextFraction // (1, 3, 2)
Fraction(23, 1, 3, 2).nextFraction // (1, 3, 7)
Fraction(23, 1, 3, 7).nextFraction // (1, 4, 1)
Fraction(23, 1, 4, 1).nextFraction // (1, 4, 7)
```

Теперь, если n - это не полный квадрат, то запрашиваем следующую итерацию дроби до тех пор,
пока следующая итерация не будет найдена среди предыдущих на i-ом месте с конца.
Это означает, что цикл замкнулся с длиной i + 1.

**Код:**

```dotty
@tailrec
def getCycleCount(memory: List[Fraction]): Int =
  memory match
    case Nil => 0
    case head :: _ =>
      val nextFraction = head.nextFraction
      val index        = memory.indexOf(nextFraction)
      if index != -1 then index + 1
      else getCycleCount(nextFraction :: memory)

def getFraction(n: Int): Int =
  if math.sqrt(n).isWhole then 0
  else getCycleCount(List(Fraction(n, 1, 0, 1)))

getFraction(1) // 0
getFraction(2) // 1
getFraction(3) // 2
getFraction(4) // 0
getFraction(5) // 1
getFraction(6) // 2
getFraction(7) // 4
getFraction(8) // 2
getFraction(9) // 0

val count = (1 to 10000).count(n => getFraction(n) % 2 == 1)
```

## Задача №65

[Задача №65 - Convergents of e](https://projecteuler.net/problem=65)

> Квадратный корень из 2 можно записать в виде бесконечной непрерывной дроби.
> 
> \\(\sqrt{2} = 1 + \frac{1}{2 + \frac{1}{2 + \frac{1}{2 + \frac{1}{2 + ...}}}}\\)
> 
> Бесконечную непрерывную дробь можно записать, воспользовавшись обозначением \\(\sqrt{2} = \[1; (2)\]\\), 
> где (2) указывает на то, что 2 повторяется **до бесконечности**. 
> Подобным образом, \\(\sqrt{23} = \[4; (1, 3, 1, 8)\]\\).
>
> Оказывается, что последовательность частичных значений непрерывных дробей предоставляет наилучшую 
> рациональную аппроксимацию квадратного корня. Рассмотрим приближения \\(\sqrt{2}\\).
>
> \\(1 + \frac{1}{2} = \frac{3}{2}\\)
> 
> \\(1 + \frac{1}{2 + \frac{1}{2}} = \frac{7}{5}\\)
> 
> \\(1 + \frac{1}{2 + \frac{1}{2 + \frac{1}{2}}} = \frac{17}{12}\\)
> 
> \\(1 + \frac{1}{2 + \frac{1}{2 + \frac{1}{2 + \frac{1}{2}}}} = \frac{41}{29}\\)
>
> Таким образом, последовательность первых десяти приближений для \\(\sqrt{2}\\) имеет вид:
> 
> \\(1, \frac{3}{2}, \frac{7}{5}, \frac{17}{12}, \frac{41}{29}, \frac{99}{70}, \frac{239}{169}, \frac{577}{408}, \frac{1393}{985}, \frac{3363}{2378}, ...\\)
> 
> Самое удивительное, что важная математическая константа
> 
> \\(e = \[2; 1, 2, 1, 1, 4, 1, 1, 6, 1, ... , 1, 2k, 1, ...\]\\).
> 
> Первые десять членов последовательности приближений для e перечислены ниже:
> 
> \\(2, 3, \frac{8}{3}, \frac{11}{4}, \frac{19}{7}, \frac{87}{32}, \frac{106}{39}, \frac{193}{71}, \frac{1264}{465}, \frac{1457}{536}, ... \\)
> 
> Сумма цифр числителя 10-го приближения равна 1 + 4 + 5 + 7 = 17.
>
> Найдите сумму цифр числителя 100-го приближения непрерывной дроби для e.

**Алгоритм:**

1. `getESequence(n: Int): IndexedSeq[Int]` - функция генерирует последовательность чисел цепных дробей, 
   которая используется для вычисления числа Эйлера.
2. `getEFractions(n: Int): (BigInt, BigInt)` - функция возвращает n-ю цепную дробь (с перевернутыми числителем и знаменателем).
   Для получения n-й цепной дроби проходим последовательность `getESequence(n)` справа, получая следующую итерацию дроби
   и переворачиваем её для следующей итерации.

**Код:**

```dotty
def getESequence(n: Int): IndexedSeq[Int] =
   if n < 3 then IndexedSeq(2, 1).take(n)
   else
     val chain = (2 to n / 3).flatMap(i => IndexedSeq(1, 1, 2 * i))
     IndexedSeq(2, 1, 2).take(n) ++ chain ++ (1 to n % 3).map(_ => 1)

def getEFractions(n: Int): (BigInt, BigInt) =
  getESequence(n).foldRight((BigInt(0), BigInt(1))) { case (i, (num, den)) =>
    (den, i * den + num)
  }

def getSumOfNum(n: Int): BigInt =
  val (_, num) = getEFractions(n)
  num.toString.map(_.getNumericValue).sum

getSumOfNum(10) // 17
```

## Задача №66

[Задача №66 - Diophantine Equation](https://projecteuler.net/problem=66)

> Рассмотрим квадратные диофантовы уравнения вида: \\(x^2 - Dy^2 = 1\\)
> 
> К примеру, для D = 13, минимальное решение x составляет \\(649^2 - 13 \times 180^2 = 1\\).
> 
> Можно убедиться в том, что не существует натуральных решений при D равном квадрату целого числа.
> 
> Найдя наименьшие значения решений x при D = {2, 3, 5, 6, 7}, мы получили следующее:
> 
> - \\(3^2 - 2 \times 2^2 = 1\\)
> - \\(2^2 - 3 \times 1^2 = 1\\)
> - \\(9^2 - 5 \times 4^2 = 1\\)
> - \\(5^2 - 6 \times 2^2 = 1\\)
> - \\(8^2 - 7 \times 3^2 = 1\\)
> 
> Таким образом, рассматривая минимальные решения x при \\(D \le 7\\), было получено наибольшее значение x при D = 5.
>
> Найдите значение \\(D \le 1000\\) для минимальных решений x, при котором получено наибольшее значение x.

**Алгоритм:**

[Метод «чакравала»](https://ru.wikipedia.org/wiki/%D0%9C%D0%B5%D1%82%D0%BE%D0%B4_%C2%AB%D1%87%D0%B0%D0%BA%D1%80%D0%B0%D0%B2%D0%B0%D0%BB%D0%B0%C2%BB) 
для решения [уравнения Пелля](https://ru.wikipedia.org/wiki/%D0%A3%D1%80%D0%B0%D0%B2%D0%BD%D0%B5%D0%BD%D0%B8%D0%B5_%D0%9F%D0%B5%D0%BB%D0%BB%D1%8F).

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

    @tailrec
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

      @tailrec
      def loop(iteration: Iteration): Option[(BigInt, BigInt)] =
        if iteration.isSuccess then Some((iteration.a, iteration.b))
        else loop(iteration.getNext)

      loop(Iteration(a, BigInt(1), k, d))

DiophantineEquation(67).minimalEquation
// Some((48842,5967))
```

Итоговое решение задачи:

**Код:**

```dotty
def getDWithLargestX(lim: Int): Int =
  (1 to lim).maxBy: d =>
    DiophantineEquation(d).minimalEquation.map(_._1).getOrElse(BigInt(0))

getDWithLargestX(7) // 5
```

## Задача №67

[Задача №67 - Maximum Path Sum II](https://projecteuler.net/problem=67)

**Алгоритм:**

Алгоритм аналогичен решению 18-й задачи.

## Задача №68

[Задача №68 - Magic 5-gon Ring](https://projecteuler.net/problem=68)

> Рассмотрим следующее "магическое" треугольное кольцо, заполненное числами от 1 до 6, с суммой на каждой линии равной 9.
> 
> ![](https://projecteuler.net/resources/images/0068_1.png?1678992052)
> 
> Проходя **по направлению часовой стрелки**, начав с группы с наименьшим внешним узлом (в данном примере: 4,3,2), 
> каждое решение можно описать единственным образом. 
> К примеру, вышеуказанное решение можно описать множеством: 4,3,2; 6,2,1; 5,1,3.
>
> Существует возможность заполнить кольцо с четырьмя различными суммами на линиях: 9, 10, 11 и 12. 
> Всего существует восемь решений.
>
> | Сумма | Множество решений   |
> |-------|---------------------|
> | 9     | 4,2,3; 5,3,1; 6,1,2 |
> | 9     | 4,3,2; 6,2,1; 5,1,3 |
> | 10    | 2,3,5; 4,5,1; 6,1,3 |
> | 10    | 2,5,3; 6,3,1; 4,1,5 |
> | 11    | 1,4,6; 3,6,2; 5,2,4 |
> | 11    | 1,6,4; 5,4,2; 3,2,6 |
> | 12    | 1,5,6; 2,6,4; 3,4,5 |
> | 12    | 1,6,5; 3,5,4; 2,4,6 |
>
> Объединяя элементы каждой группы, можно образовать 9-тизначную строку. 
> Максимальное значение такой строки для треугольного кольца составляет 432621513.
>
> Используя числа от 1 до 10, в зависимости от расположения, можно образовать 16-тизначные и 17-тизначные строки. 
> Каково максимальное значение **16-тизначной** строки для "магического" пятиугольного кольца?

**Алгоритм:**

Определим, что "магическое" пятиугольное кольцо задается соотношением 
\\(a \to b \to c \Rightarrow d \to c \to e \Rightarrow f \to e \to g \Rightarrow h \to g \to i \Rightarrow j \to i \to b\\), где

- (a, b, c, d, e, f, g, h, i, j) - различные числа от 1 до 10
- a + b + c = d + c + e = f + e + g = h + g + i = j + i + b. Следовательно:
    - e = a + b - d
    - g = d + c - f
    - i = f + e - h
    - h + g == b + j 
    - j + i == a + c

Условия нахождения максимального 16-значного кольца говорит о том, что 10 должна находиться на "внешней вершине",
иначе она "учитывалась" бы дважды и получилось бы 17-е число.
Т.к. решение не зависит от "вращения" кольца, то можно предположить a равным 10,
т.е. начать поиск решения с "десятки".

Учитывая предыдущие ограничения перебор может быть таким:

**Код:**

```dotty
def solutions: IndexedSeq[Long] =
  val a = 10
  for
    b <- 1 to 9
    c <- 1 to 9
    if c != b
    d <- 1 to 9
    if d != b && d != c
    e = 10 + b - d
    if 1 <= e && e <= 9 && e != b && e != c && e != d
    f <- 1 to 9
    if f != b && f != c && f != d && f != e
    g = d + c - f
    if 1 <= g && g <= 9 && g != b && g != c && g != d && g != e && g != f
    h <- 1 to 9
    if h != b && h != c && h != d && h != e && h != f && h != g
    i = f + e - h
    if 1 <= i && i <= 9 && i != b && i != c && i != d && i != e && i != f && i != g && i != h
    j = 45 - b - c - d - e - f - g - h - i
    if j != b && j != c && j != d && j != e && j != f && j != g && j != h && j != i
    if h + g == b + j && j + i == 10 + c
  yield
    val min = math.min(math.min(d, f), math.min(h, j))
    if min == d then s"$d$c$e$f$e$g$h$g$i$j$i$b$a$b$c".toLong
    else if min == f then s"$f$e$g$h$g$i$j$i$b$a$b$c$d$c$e".toLong
    else if min == h then s"$h$g$i$j$i$b$a$b$c$d$c$e$f$e$g".toLong
    else s"$j$i$b$a$b$c$d$c$e$f$e$g$h$g$i".toLong
  end for
```

## Задача №69

[Задача №69 - Totient Maximum](https://projecteuler.net/problem=69)

> Функция Эйлера, \\(\phi(n)\\) (иногда ее называют фи-функцией) используется для определения количества чисел, 
> меньших n, которые взаимно просты с n. 
> К примеру, т.к. 1, 2, 4, 5, 7 и 8 меньше девяти и взаимно просты с девятью, \\(\phi(9)=6\\).
>
> | n  | Взаимно простые числа | phi(n) | n/phi(n)  |
> |----|-----------------------|--------|-----------|
> | 2  | 1                     | 1      | 2         |
> | 3  | 1,2                   | 2      | 1.5       |
> | 4  | 1,3                   | 2      | 2         |
> | 5  | 1,2,3,4               | 4      | 1.25      |
> | 6  | 1,5                   | 2      | 3         |
> | 7  | 1,2,3,4,5,6           | 6      | 1.1666... |
> | 8  | 1,3,5,7               | 4      | 2         | 
> | 9  | 1,2,4,5,7,8           | 6      | 1.5       |
> | 10 | 1,3,7,9               | 4      | 2.5       |
> 
> Нетрудно заметить, что максимум \\(n/\phi(n)\\) наблюдается при n = 6, для \\(n \leq 10\\).
> 
> Найдите значение \\(n \leq 1000000\\), при котором значение \\(n/\phi(n)\\) максимально.

**Алгоритм:**

Согласно [функции Эйлера от натурального числа](https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D1%8F_%D0%AD%D0%B9%D0%BB%D0%B5%D1%80%D0%B0):
\\(\varphi (n)=n \prod _{p\mid n} \left( 1 - {\frac {1}{p}}\right), n > 1\\).

Это означает, что \\(\frac{n }{\varphi (n)} = \prod _{p\mid n} \left({\frac {p}{p - 1}}\right)\\).

Получается, что на соотношение \\(\frac{n }{\varphi (n)}\\) не влияет то, с какой степенью в n входит p.
Т.е. для \\(m = p\_1^{a\_1} \times p\_2^{a\_2} \times ... \times p\_k^{a\_k} \\) 
и \\(o = p\_1 \times p\_2 \times ... \times p\_k \\) выполняется \\(\frac{m}{\varphi (m)} = \frac{o}{\varphi (o)}\\). 
Поэтому можно рассматривать только те числа, которые являются произведениями различных простых чисел.

Учитывая, что \\(\frac{p\_i}{p\_i - 1} > \frac{p\_j}{p\_j - 1} \\) если \\(p\_i < p\_j\\),
максимальное соотношение \\(\frac{n }{\varphi (n)}\\) среди всех чисел менее limit
будет для такого \\(n = p\_1 \times p\_2 \times ... \times p\_k \\) меньшего limit, 
которое является произведением наименьших возможных различных простых чисел (\\(p\_1 = 2, p\_2 = 3, p\_3 = 5\\) и т.д.),
при том, что \\(n * p\_{k + 1}\\) уже становится больше limit.

Т.к. для всех остальных чисел \\(m = p\_1^{a\_1} \times p\_2^{a\_2} \times ... \times p\_q^{a\_q}\\) меньших limit
следует:

- \\(q \leq k\\), т.к. иначе \\(n * p\_{k + 1}\\) было бы меньше limit
- \\(\frac{m}{\varphi (m)} \leq \frac{n}{\varphi (n)}\\), т.к. для всех \\(1 \leq i \leq q\\) выполняется
  \\(\frac{p\_i}{p\_i - 1} \leq \frac{ithp}{ithp - 1}\\), где ithp - i-е наименьшее простое число.

**Код:**

```dotty
def isPrime(n: Long): Boolean = ... // Из Problem 27

def nextPrime(n: Long): Long =
  if !isPrime(n) then 2
  else if n == 2 then 3
  else if n == 3 then 5
  else
    var nextPrime = if n % 3 == 1 then n + 4 else n + 2
    while !isPrime(nextPrime) do
      nextPrime = if nextPrime % 3 == 1 then nextPrime + 4 else nextPrime + 2
    nextPrime

def getMax(limit: Long): Long =
  @tailrec
  def loop(acc: Long, p: Long): Long =
    if acc * p > limit then acc
    else loop(acc * p, nextPrime(p))

  loop(1, 2)

getMax(10) // 6
```

## Задача №70

[Задача №70 - Totient Permutation](https://projecteuler.net/problem=70)

> Функция Эйлера, \\(\phi(n)\\) (иногда ее называют фи-функцией) используется для определения количества чисел, 
> меньших n, которые взаимно просты с n. 
> К примеру, т.к. 1, 2, 4, 5, 7 и 8 меньше девяти и взаимно просты с девятью, \\(\phi(9)=6\\).
> 
> Число 1 считается взаимно простым для любого положительного числа, так что \\(\phi(1)=1\\).
>
> Интересно, что \\(\phi(87109)=79180\\), и, как можно заметить, 87109 является перестановкой 79180.
>
> Найдите такое значение n, \\(1 \lt n \lt 10^7\\), при котором \\(\phi(n)\\) является перестановкой n, 
> а отношение \\(n/\phi(n)\\) является минимальным.

**Алгоритм:**

Согласно [алгоритму расчета функции Эйлера](../../algorithms/algorithms/number_theory/primes.md) 
создается массив всех значений функции Эйлера менее 10 миллионов.
Затем перебираются все варианты, удовлетворяющие условию задачи.

**Код:**

```dotty
def totientArray(limit: Int): Array[Long] =
  val phiArray = new Array[Long](limit + 1)
  phiArray(1) = 1

  val primesArray = primesNoMoreThanN(limit)
  for n <- 1 to limit / 2 do
    var i = 0
    while i < primesArray.length && n * primesArray(i) <= limit do
      val p = primesArray(i)
      phiArray(n * p) = phiArray(n) * (if n % p == 0 then p else p - 1)
      i += 1

  phiArray
  
def getMin(limit: Int): Int =
  val totients = totientArray(limit)
  (2 until limit).minBy: n =>
    val phi = totients(n)
    if phi.toString.sorted == n.toString.sorted then n.toDouble / phi
    else Int.MaxValue
```

## Задача №71

[Задача №71 - Ordered Fractions](https://projecteuler.net/problem=71)

> Рассмотрим дробь \\(\dfrac n d\\), где n и d являются натуральными числами. 
> Если \\(n \lt d\\) и \\(\operatorname{HCF}(n,d)=1\\), то речь идет о сокращенной правильной дроби.
>
> Если перечислить множество сокращенных правильных дробей для d ≤ 8 в порядке возрастания их значений, получим:
>
> \\(\frac 1 8, \frac 1 7, \frac 1 6, \frac 1 5, \frac 1 4, \frac 2 7, \frac 1 3, \frac 3 8, \frac 2 5, \frac 3 7, \frac 1 2, \frac 4 7, \frac 3 5, \frac 5 8, \frac 2 3, \frac 5 7, \frac 3 4, \frac 4 5, \frac 5 6, \frac 6 7, \frac 7 8\\)
>
> Нетрудно заметить, что дробь \\(\dfrac 2 5\\) расположена непосредственно слева от дроби \\(\dfrac 3 7\\).
>
> Перечислив множество сокращенных правильных дробей для \\(d \le 1000000\\) в порядке возрастания их значений, 
> найдите числитель дроби, расположенной непосредственно слева от дроби \\(\dfrac 3 7\\).

**Алгоритм:**

1. Допустим известен знаменатель дроби \\(d\_1\\). Чему равен числитель \\(n\_1\\)?
   \\(\frac{n\_1}{d\_1} < \frac{3}{7} \Rightarrow n\_1 < \frac{3*d\_1}{7}\\)
2. Остается перебрать все знаменатели от 2 до d.

**Код:**

```dotty
def getFractionByD(d: Int): Int =
  val n = 3.0 * d / 7
  if n.isWhole then n.toInt - 1 else n.toInt

def getFraction(d: Int): (Int, Int) =
  (2 to d).foldLeft((0, 1)) { case ((n1, d1), d2) =>
    val n2 = getFractionByD(d2)
    if n1 * d2 <= n2 * d1 then (n2, d2) else (n1, d1)
  }

getFraction(8) // (2, 5)
```

## Задача №72

[Задача №72 - Counting Fractions](https://projecteuler.net/problem=72)

> Рассмотрим дробь \\(\dfrac n d\\), где n и d являются натуральными числами. 
> Если \\(n \lt d\\) и \\(\operatorname{HCF}(n,d)=1\\), то речь идет о сокращенной правильной дроби.
>
> Если перечислить множество сокращенных правильных дробей для d ≤ 8 в порядке возрастания их значений, получим:
>
> \\(\frac 1 8, \frac 1 7, \frac 1 6, \frac 1 5, \frac 1 4, \frac 2 7, \frac 1 3, \frac 3 8, \frac 2 5, \frac 3 7, \frac 1 2, \frac 4 7, \frac 3 5, \frac 5 8, \frac 2 3, \frac 5 7, \frac 3 4, \frac 4 5, \frac 5 6, \frac 6 7, \frac 7 8\\)
>
> Нетрудно заметить, что данное множество состоит из 21 элемента.
> 
> Из скольки элементов будет состоять множество сокращенных правильных дробей для \\(d \le 1000000\\)?

**Алгоритм:**

Достаточно подсчитать функцию Эйлера для каждого числа от 2 до миллиона и сложить все вместе.

**Код:**

```dotty
def getCount(d: Int): Long =
  val totients = totientArray(d) // Из Problem 70
  totients.sum - totients(1)

getCount(8) // 21
```

## Задача №73

[Задача №73 - Counting Fractions in a Range](https://projecteuler.net/problem=73)

> Рассмотрим дробь \\(\dfrac n d\\), где n и d являются натуральными числами. 
> Если \\(n \lt d\\) и \\(\operatorname{HCF}(n,d)=1\\), то речь идет о сокращенной правильной дроби.
> 
> Если перечислить множество сокращенных правильных дробей для \\(d \le 8\\) в порядке возрастания их значений, получим:
>
> \\(\frac 1 8, \frac 1 7, \frac 1 6, \frac 1 5, \frac 1 4, \frac 2 7, \frac 1 3, \frac 3 8, \frac 2 5, \frac 3 7, \frac 1 2, \frac 4 7, \frac 3 5, \frac 5 8, \frac 2 3, \frac 5 7, \frac 3 4, \frac 4 5, \frac 5 6, \frac 6 7, \frac 7 8\\)
> 
> Нетрудно заметить, между дробями \\(\dfrac 1 3\\) и \\(\dfrac 1 2\\) расположены 3 другие дроби.
> 
> Сколько дробей расположено между \\(\dfrac 1 3\\) и \\(\dfrac 1 2\\) в упорядоченном множестве 
> сокращенных правильных дробей для \\(d \le 12000\\)?

**Алгоритм:**

Необходимо найти для всех знаменателей (d) от 5 до лимита, количество числителей (n), взаимно простых с d, 
таких что \\(\frac d 3 < n < \frac d 2\\). 

**Код:**

```dotty
@tailrec
def gcd(a: Long, b: Long): Long =
  if b == 0 then a else gcd(b, a % b)

def getCountForD(d: Int): Int =
  val start = (d / 3.0).toInt + 1
  val finish0 = d / 2.0
  val finish = if finish0.isWhole then finish0.toInt - 1 else finish0.toInt
  (start to finish).count(n => gcd(n, d) == 1)

def getCount(limit: Int): Long = (5 to limit).map(getCountForD).sum

getCount(8) // 3
```

## Задача №74

[Задача №74 - Digit Factorial Chains](https://projecteuler.net/problem=74)

> Число 145 известно благодаря такому свойству, что сумма факториалов его цифр равна 145:
> 1! + 4! + 5! = 1 + 24 + 120 = 145
> 
> Возможно, менее известно число 169 - оно создает самую длинную цепочку чисел, которая возвращается к 169. 
> Оказывается, существует только три таких замкнутых цепи:
>
> - \\(169 \to 363601 \to 1454 \to 169\\)
> - \\(871 \to 45361 \to 871\\)
> - \\(872 \to 45362 \to 872\\)
> 
> Нетрудно показать, что ЛЮБОЕ начальное число рано или поздно приведет к замыканию цепи. Например,
>
> - \\(69 \to 363600 \to 1454 \to 169 \to 363601 (\to 1454)\\)
> - \\(78 \to 45360 \to 871 \to 45361 (\to 871)\\)
> - \\(540 \to 145 (\to 145)\\)
> 
> Начав с 69, мы получим цепь из пяти неповторяющхися членов, но самая длинная цепь, 
> начинающаяся с числа меньше миллиона, имеет шестьдесят неповторяющихся членов.
> 
> Сколько существует цепей, начинающихся с числа меньше миллиона, содержащих ровно шестьдесят неповторяющихся членов?

**Алгоритм:**

Подсчет "цепочки" для одного числа большой проблемы не доставляет. 
Но в случае миллиона чисел грубый подсчет не подойдет, т.к. он многократно увеличивает необходимость
считать "цепочку" для одного и того же числа. Например, для чисел 69, 363600 и 1454 придется три раза вычислять цепочку
для 1454. Поэтому необходимо сохранять в памяти подсчитанные значения для последующего переиспользования.

Это можно сделать с помощью [Функционального состояния](../../fp/state.md).

Для начала определим функцию, подсчитывающую сумму факториалов цифр заданного числа:

- запомним факториалы всех цифр в коллекции `factorials`

**Код:**

```dotty
val factorials = (0 to 9).map(i => (2 to i).product)
@tailrec
def getDigitFactorials(n: Int, acc: Int = 0): Int =
  val newAcc = acc + factorials(n % 10)
  if n < 10 then newAcc
  else getDigitFactorials(n / 10, newAcc)
  
getDigitFactorials(145) // 145  
```

Определим тип `Memory` для хранения значений длины "цепочки" для заданных чисел.
И `MemoryState` - как вычисление нового состояния по переданному числу, для которого необходимо вычистить длину цепочки.

```dotty
opaque type Memory = Map[Int, Int]
private val initialMemory: Memory = Map.empty[Int, Int]

opaque type MemoryState = Memory => (Int, Memory)
```

Опередим саму "цепочку" `Chain` как цепочку чисел, имеющих начальное значение, текущее, а также всю цепочку.
Например, цепочка `(145)` будет задаваться как `Chain(145, 145, List(145))` - цепочка длины 1 с начальным и конечным
значением равным 145. При этом конструктор сделаем приватным, чтобы цепочку можно было создавать только по
начальному значению. Это гарантирует, что цепочка в любой момент времени не пустая,
т.к. создать её можно только по начальному значению и изменять её можно только добавляя звенья цепи.

```dotty
final case class Chain private (initial: Int, current: Int, links: List[Int]):
  def add(number: Int): Chain =
    Chain(initial, number, number :: links)

object Chain:
  def apply(initial: Int): Chain =
    Chain(initial, initial, List(initial))
```

Определим метод `getChainLoop`, который для заданной цепочки обновляет состояние памяти:

Если для текущего звена цепи в памяти уже сохранено значение, например, для цепи \\(69 \to 363600 \to 1454\\)
в памяти нашлось значение для 1454, то значит, мы наткнулись на ранее подсчитанный цикл,
в который не входит "хвост" цепи (69 и 363600) 
(иначе значения из хвоста были бы внутри цикла и тоже были бы сохранены в памяти).
Это означает, что необходимо добавить "хвост" цепи в память: 
для каждого следующего числа из хвоста значение длины цепи будет на 1 больше предыдущего значения.
Обновленную память и значение для первого звена цепи возвращаем в качестве обновленного состояния.

```dotty
final case class Chain private (initial: Int, current: Int, links: List[Int]):
  ...

  def addTailToMemory(count: Int): MemoryState = memory =>
    val updatedMemory =
      links.zipWithIndex.tail.foldLeft(memory) {
        case (acc, (item, index)) =>
          acc + (item -> (count + index))
      }
    (updatedMemory(initial), updatedMemory)
end Chain   
```

```dotty
def getChainLoop(chain: Chain): MemoryState = memory =>
  if memory.contains(chain.current) then
    val count = memory(chain.current)
    chain.addTailToMemory(count)(memory)
  else
    ???
```

Если значения длины цепочки для текущего звена не найдено, то вычисляем следующее звено цепи - `next`.
Если `next` - новое звено цепи и его нет в текущей цепочке, то значит, до цикла мы ещё не добрались,
поэтому вызываем `getChainLoop` для новой цепочки с добавленным звеном `next`.

```dotty
def getChainLoop(chain: Chain): MemoryState = memory =>
  if memory.contains(chain.current) then
    ...
  else
    val next = getDigitFactorials(chain.current)
    val idx = chain.links.indexOf(next)
    if idx == -1 then getChainLoop(chain.add(next))(memory)
    else ???
```

Если же следующего звено уже есть в цепочке, то мы наткнулись на цикл, который ещё не сохранен в памяти.
Необходимо этот цикл рассчитать и обновить в памяти.

Например, цепочка \\(69 \to 363600 \to 1454 \to 169 \to 363601\\) приводит к следующему элементу 1454,
который уже есть в цепи.

- Если следующий элемент найден по индексу `idx`, то это означает, что первые `idx + 1` звеньев цепи составляют цикл,
  а последующий звенья цепи лишь приводят к этому циклу.
- Для звеньев цепи из цикла длина "цепи" равна длине цикла, т.е. `idx + 1`.
- Для последующих звеньев - на 1 больше, чем длина цепи предыдущего звена.

```dotty
final case class Chain private (initial: Int, current: Int, links: List[Int]):
  ...

  def addAllChainToMemory(cycleIndex: Int): MemoryState = memory =>
    val updatedMemory =
      links.zipWithIndex.foldLeft(memory) {
        case (acc, (item, index)) =>
          val count = math.max(cycleIndex, index)
          acc + (item -> (count + 1))
      }

    (updatedMemory(initial), updatedMemory)
end Chain
```

Итого, `getChainLoop` примет вид:

```dotty
def getChainLoop(chain: Chain): MemoryState = memory =>
  if memory.contains(chain.current) then
    val count = memory(chain.current)
    chain.addTailToMemory(count)(memory)
  else
    val next = getDigitFactorials(chain.current)
    val idx  = chain.links.indexOf(next)
    if idx == -1 then getChainLoop(chain.add(next))(memory)
    else chain.addAllChainToMemory(idx)(memory)
```

Для отдельно заданного значения длину цепи можно подсчитать так:

```dotty
def getChain(n: Int): Int =
  getChainLoop(Chain(n))(initialMemory)._1
```

Но этот способ не использует накопленные знания в памяти, получая на входе пустую память. Он неэффективен.

Создадим метод `getMemoryState` для получения `MemoryState` по заданному числу, 
для которого рассчитывается длина цепи с использованием памяти.

```dotty
opaque type MemoryState = Memory => (Int, Memory)

object MemoryState:
  extension (underlying: MemoryState)
    def run(s: Memory): (Int, Memory) = underlying(s)
    
def getMemoryState(n: Int): MemoryState =
  getChainLoop(Chain(n))  
  
val (count, memory) = getMemoryState(169).run(initialMemory)
// (3, Map(1454 -> 3, 363601 -> 3, 169 -> 3))  

val (count1, memory1) = getMemoryState(363600).run(memory)
// (3, Map(1454 -> 3, 363601 -> 3, 169 -> 3, 363600 -> 4))   
```

В результате подсчет количества чисел, для которых длина цепочки равна 60, с использованием памяти 
для сохранения уже посчитанных значений и с использованием методов функционального программирования выглядит так:

```dotty
val limit = 1_000_000
val (_, result) =
  (1 until limit)
    .foldLeft((initialMemory, 0)) { case ((memory, acc), i) =>
      val (count, newMemory) = getMemoryState(i).run(memory)
      val newAcc             = if count == 60 then acc + 1 else acc
      (newMemory, newAcc)
    }
    
result // Ответ по задаче
```

Подсчет для миллиона чисел выполняется за 2 секунды.

## Задача №75

[Задача №75 - Singular Integer Right Triangles](https://projecteuler.net/problem=75)

> Оказывается, что \\(12 cm\\) - наименьшая длина проволоки, сгибая которую, 
> можно получить прямоугольный треугольник с целыми сторонами, притом лишь единственным способом. 
> Есть и другие примеры.
>
> - \\(12 cm\\): \\((3,4,5)\\)
> - \\(24 cm\\): \\((6,8,10)\\)
> - \\(30 cm\\): \\((5,12,13)\\)
> - \\(36 cm\\): \\((9,12,15)\\)
> - \\(40 cm\\): \\((8,15,17)\\)
> - \\(48 cm\\): \\((12,16,20)\\)
> 
> В противоположность этим примерам, существуют такие длины проволоки (к примеру, \\(20 cm\\)), 
> из которых нельзя получить прямоугольный треугольник с целыми сторонами. 
> Другие же длины позволяют найти несколько возможных решений: 
> к примеру, сгибая проволоку длинной \\(120 cm\\), можно получить ровно 
> три различных прямоугольных треугольника с целыми сторонами.
> \\(120 cm\\): \\((30,40,50)\\), \\((20,48,52)\\), \\((24,45,51)\\)
> 
> Известно, что длина проволоки составляет **L**. 
> Для скольких значений \\(L \le 1500000\\), сгибая проволоку, 
> можно получить ровно один прямоугольный треугольник с целыми сторонами?

**Алгоритм:**

Нахождение пифагоровых троек с заданной суммой [описано в соответствующем разделе](../../algorithms/algorithms/fibonacci.md).

**Код:**

```dotty
@tailrec
def gcd(a: Long, b: Long): Long =
  if b == 0 then a else gcd(b, a % b)

def isExactlyOneWay(sum: Long): Boolean =
  val s2     = sum / 2
  val sqrt   = math.sqrt(s2.toDouble).toLong
  val mLimit = if sqrt * sqrt == s2 then sqrt - 1 else sqrt

  @tailrec
  def loop(m: Long, count: Int): Int =
    if m > mLimit || count > 1 then count
    else if s2 % m == 0 then
      var sm = s2 / m
      while sm % 2 == 0 do sm /= 2
      val startK = if m % 2 == 1 then m + 2 else m + 1
      val endK   = math.min(2 * m, sm + 1)
      val ncount =
        (startK until endK by 2).count(k => sm % k == 0 && gcd(k, m) == 1)
      loop(m + 1, count + ncount)
    else loop(m + 1, count)

  loop(m = 2L, count = 0) == 1

end isExactlyOneWay

val count = (2 to 1500000 by 2).count(l => isExactlyOneWay(l))
```

## Задача №76

[Задача №76 - Counting Summations](https://projecteuler.net/problem=76)

> Число 5 можно записать в виде суммы ровно шестью различными способами:
>
> - 4 + 1
> - 3 + 2
> - 3 + 1 + 1
> - 2 + 2 + 1
> - 2 + 1 + 1 + 1
> - 1 + 1 + 1 + 1 + 1
>
> Сколькими различными способами можно записать число 100 в виде суммы по крайней мере двух натуральных чисел?

**Алгоритм:**

Это [классическая задача о нахождении количества партиций](../../algorithms/algorithms/combinatorics/permutation.md) -
количество способов записи n в виде суммы натуральных чисел.
Единственное изменение - не подходит случай записи числа в виде суммы из одного элемента - себя самого.
Поэтому итоговый ответ - число партиций минус 1.

**Код:**

```dotty
def partition(number: Int): BigInt =
  val array = new Array[BigInt](number + 1)
  array(0) = BigInt(1)
  if number >= 1 then array(1) = BigInt(1)
  if number >= 2 then array(2) = BigInt(2)

  def partitionPart(s: Int, n: Int, pS: BigInt): BigInt =
    var k  = s
    var op = k * (3 * k - 1) / 2
    var p  = pS
    while op <= n do
      if (k + 1) % 2 == 0 then
        p += array(n - op)
      else
        p -= array(n - op)
      k += s
      op = k * (3 * k - 1) / 2
    p

  for n <- 3 to number do
    val p = partitionPart(1, n, BigInt(0))
    array(n) = partitionPart(-1, n, p)
  array(number)
end partition

def optionsToGetSumAsASumOfAtLeastTwoPositiveNumbers(sum: Int): BigInt =
  partition(sum) - 1

optionsToGetSumAsASumOfAtLeastTwoPositiveNumbers(5) // 6
```

## Задача №77

[Задача №77 - Prime Summations](https://projecteuler.net/problem=77)

> Число 10 можно записать в виде суммы простых чисел ровно пятью различными способами:
> 
> - 7 + 3
> - 5 + 5
> - 5 + 3 + 2
> - 3 + 3 + 2 + 2
> - 2 + 2 + 2 + 2 + 2
>
> Какое наименьшее число можно записать в виде суммы простых чисел по крайней мере пятью тысячами различных способов?

**Алгоритм:**

- Составим "достаточно большой" список простых чисел (достаточно взять все простые числа меньше 100)
- Дальше - классическая задачка о том, сколькими способами можно собрать заданную сумму исходя из 
  заданного набор монет - эта задача обсуждается в Problem 31
- Найти первое число, которое можно собрать более чем 5000 способами

**Код:**

```dotty
def primesNoMoreThanN(n: Int): Array[Int] = ... // Из Problem 27

lazy val primes = primesNoMoreThanN(100)

def countWays(coins: Array[Int], sum: Int): Long = ... // Из Problem 31

countWays(primes, 10) // 5

val count = LazyList
  .from(1)
  .find(i => countWays(primes, i) >= 5000)
  .getOrElse(-1)
```

## Задача №78

[Задача №78 - Coin Partitions](https://projecteuler.net/problem=78)

> Пусть p(n) представляет собой число различных способов, которыми можно разделить монеты на несколько столбиков. 
> К примеру, пять монет можно разделить на несколько столбиков ровно семью различными способами, таким образом p(5) = 7.
>
> - OOOOO
> - OOOO   O
> - OOO   OO
> - OOO   O   O
> - OO   OO   O
> - OO   O   O   O
> - O   O   O   O   O
>
> Найдите наименьшее значение n, для которого p(n) делится на один миллион без остатка.

**Алгоритм:**

Как и в Problem 76 - это [классическая задача о нахождении количества партиций](../../algorithms/algorithms/combinatorics/permutation.md) -
количество способов записи n в виде суммы натуральных чисел.

В данном случае необходимо составить "достаточно большой" массив значений партиций (100000 достаточно).
А затем найти в массиве первый индекс, значение по которому делится на миллион.

**Код:**

```dotty
def partitionArray(number: Int): Array[BigInt] =
  val array = new Array[BigInt](number + 1)
  array(0) = BigInt(1)
  array(1) = BigInt(1)
  array(2) = BigInt(2)

  def partitionPart(s: Int, n: Int, pS: BigInt): BigInt =
    var k  = s
    var op = k * (3 * k - 1) / 2
    var p  = pS
    while op <= n do
      if (k + 1) % 2 == 0 then
        p += array(n - op)
      else
        p -= array(n - op)
      k += s
      op = k * (3 * k - 1) / 2
    p

  for n <- 3 to number do
    val p = partitionPart(1, n, BigInt(0))
    array(n) = partitionPart(-1, n, p)
  array
end partitionArray

def findFirst(n: Int): Option[Int] =
  val arr = partitionArray(n)
  arr.indices.find(i => arr(i) % 1000000 == 0)
```

## Задача №79

[Задача №79 - Passcode Derivation](https://projecteuler.net/problem=79)

> Для проведения операций с банковскими счетами онлайн распространен метод безопасности, 
> заключающийся в том, что пользователя просят указать три случайные символа его кода доступа. 
> К примеру, если код доступа пользователя 531278, у него могут попросить ввести 2-й, 3-й и 5-й символы, 
> и ожидаемым ответом будет 317.
> 
> В текстовом файле keylog.txt содержится 50 удачных попыток авторизации.
> 
> Учитывая, что три символа кода всегда запрашивают по их порядку в коде, 
> проанализируйте файл с целью определения наиболее короткого секретного кода доступа неизвестной длины.

**Алгоритм:**

1. `getRestNumbers` - функция, принимающая на вход последовательность строк `numbers` и символ `digit`. 
   Возвращает новую последовательность строк, в которой каждая строка начинается с символа `digit`, 
   если она уже начинается с этого символа, иначе она остается неизменной. 
   Затем фильтрует эту последовательность, удаляя пустые строки и 
   строки, которые являются подстроками других строк в последовательности.

2. `getMinCode` - функция, реализующая алгоритм для нахождения минимальной последовательности цифр,
   в которую "входит" каждое число из `numbers` ("входит" означает, что присутствуют все цифры из числа в том же порядке,
   но, возможно, со вставками других чисел: например, 680 входит в 61890 (цифры на 1, 3, 5 местах)). 
   Если длина последовательности меньше или равна 1, то возвращается первый элемент последовательности 
   или пустая строка, если последовательность пуста. 
   Если во всех числах цифры разные, то возвращается объединение чисел - не имеет значения, в каком порядке объединять,
   т.к. более короткий вариант получить не удастся - решение должно содержать все цифры.
   Если все символы в кандидате уникальны, то он и является минимальным кодом. 
   В противном случае функция находит все уникальные первые символы в строках и для каждого из них формирует 
   "оставшиеся" числа (`getRestNumbers`), и рекурсивно вызывает `getMinCode` для этих чисел. 
   Результаты этих вызовов объединяются с первым символом, и функция возвращает строку с минимальной длиной.

**Код:**

```dotty
def getRestNumbers(numbers: Seq[String], digit: Char): Seq[String] =
  val restNumbers = numbers.map: number =>
    if number.head == digit then number.tail else number

  restNumbers.filterNot: number =>
    number.isEmpty ||
      restNumbers.exists: restNumber =>
        restNumber != number && restNumber.contains(number)
  .distinct

def getMinCode(numbers: Seq[String]): String =
  if numbers.length <= 1 then
    numbers.headOption.getOrElse("")
  else
    val candidate = numbers.mkString
    if candidate.distinct.length == candidate.length then candidate
    else
      val firstDigits = numbers.map(_(0)).distinct
      firstDigits
        .map: digit =>
          val restNumbers = getRestNumbers(numbers, digit)
          s"$digit${getMinCode(restNumbers)}"
        .minBy(_.length)

getMinCode(Seq("680", "180", "690")) 
// "61890"

getMinCode(Seq("319", "680", "180", "690", "129", "620", "762")) 
// "31768290"
```

## Задача №80

[Задача №80 - Square Root Digital Expansion](https://projecteuler.net/problem=80)

> Как известно, если квадратный корень натурального числа не является целым числом, 
> то он является иррациональным числом. Разложение таких квадратных корней на десятичные дроби бесконечно 
> и не имеет никакой повторяющейся последовательности.
> 
> Квадратный корень из двух равен 1.41421356237309504880..., а сумма его первых ста цифр в десятичном представлении равна 475.
> 
> Найдите общую сумму первых ста цифр всех иррациональных квадратных корней среди первых ста натуральных чисел.

**Алгоритм:**

Алгоритм вычисления квадратного корня [методом Ньютона описан в соответствующем разделе](../../algorithms/algorithms/number_theory/numerical.md).

**Код:**

```dotty
def sqrt(n: BigInt): BigInt =
  var a = BigInt(1)
  var b = (n >> 5) + BigInt(8)
  while b >= a
  do
    val mid = (a + b) >> 1
    if mid * mid > n then b = mid - 1 else a = mid + 1
  a - 1

def getFirst100Sum(n: Int): Int =
  val s = math.sqrt(n)
  if s.isWhole then 0
  else
    val sbi = sqrt(BigInt(n) * BigInt(10).pow(10000))
    sbi.toString.take(100).map(_.getNumericValue).sum

getFirst100Sum(2) // 475
```

---

**Ссылки:**

- [Project Euler site][euler]
- [Project Euler chat][euler_chat]
- [Проект Эйлера на русском](https://euler.jakumo.org/problems.html)

[euler]: https://projecteuler.net
[euler_chat]: https://projecteuler.chat
