# Численные алгоритмы


## Наибольший общий делитель

### Алгоритм Евклида

Наибольший общий делитель (НОД) с помощью алгоритма Евклида вычисляется исходя из следующих соображений:
- `НОД(r, 0) = r` для любого ненулевого `r` (так как 0 делится на любое целое число).
- Пусть `a = b*q + r`, тогда `НОД (a, b) = НОД (b, r)`.


```scala
@tailrec
def gcdByEuclideanAlgorithm(a: Long, b: Long): Long =
  if b == 0 then a
  else gcdByEuclideanAlgorithm(b, a % b)
  
gcdByEuclideanAlgorithm(4851, 3003) // 231  
```

### Алгоритм НОД на основе четности чисел

Алгоритм, нахождения НОД, учитывающий четность чисел:
- Если `u == v`, то `НОД(u, v) = u`.
- `НОД(r, 0) = r` для любого ненулевого `r` (так как 0 делится на любое целое число).
- Если `u` и `v` - четные, то `НОД(u, v) = 2 * НОД(u/2, v/2)`.
- Если числа имеют разную четность, то НОД этих двух чисел равен НОД нечетному и четному, разделенному на 2.
- Если оба нечетные, то можно воспользоваться алгоритмом Евклида.

```scala
def gcd(a: Long, b: Long): Long =
  val u = math.abs(a)
  val v = math.abs(b)
  if u == v then u
  else if u == 0 then v
  else if v == 0 then u
  else
    (~u & 1, ~v & 1) match
      case (1, 1) => gcd(u >> 1, v >> 1) << 1
      case (1, 0) => gcd(u >> 1, v)
      case (0, 1) => gcd(u, v >> 1)
      case (_, _) => if (u > v) gcd(u - v, v) else gcd(v - u, u)
  
gcd(4851, 3003) // 231  
```


## Рандомизация данных - Линейный конгруэнтный генератор

Линейный конгруэнтный генератор использует следующую зависимость для формирования псевдослучайных величин:

X<sub>n+1</sub> = (A * X<sub>n</sub> + B) % M, где A, B и M - константы.

Величина X<sub>0</sub> называется _начальным_ числом.

Генератор можно реализовать на Scala так:

```scala
val A = 7
val B = 5
val M = 11
val generator = LazyList.iterate(0)(x => (A * x + B) % M)
generator.take(11).toList
// val res0: List[Int] = List(0, 5, 7, 10, 9, 2, 8, 6, 3, 4, 0)
```


## Преобразование десятичного числа в двоичное

Мы переводим числа с основанием 10 в числа с основанием 2.
В этих двух системах счисления процедура счета различается главным образом из-за используемых символов.

Сначала опишем алгоритм конвертации:

1. Начиная с заданного числа, составить последовательность чисел таким образом, чтобы последующее число было половиной предыдущего,
   отбрасывая десятичную часть. Продолжить до тех пор, пока последний элемент не будет удовлетворять условию 2 > x > 0,
   где x — последнее число в последовательности.
   Формально S = {x<sub>1</sub>, x<sub>2</sub>,..., x<sub>n</sub>},
   где x<sub>2</sub> = x<sub>1</sub>/2, x<sub>3</sub> = x<sub>2</sub>/2 и т.д., и 2 > x<sub>n</sub> > 0.
2. Для каждого числа из приведенного выше списка разделить на 2 и хранить остаток в контейнере.
3. Теперь контейнер содержит двоичные эквивалентные биты в обратном порядке, b<sub>1</sub>b<sub>2</sub>...b<sub>n</sub>,
   поэтому надо изменить порядок, чтобы получить двоичное эквивалентное число, b<sub>n</sub>...b<sub>2</sub>b<sub>1</sub>.

Реализация алгоритма:

```scala
def decToBinConv(x: Int): String =
  val seqOfDivByTwo = Iterator.iterate(x)(a => a / 2)
  val binList = seqOfDivByTwo
    .takeWhile(a => a > 0)
    .map(a => a % 2)
  binList.mkString.reverse
```


## Возведение числа в степень

Возведение числа в степень основывается на двух ключевых формулах:
- A<sup>2*M</sup> = (A<sup>M</sup>)<sup>2</sup>
- A<sup>M+N</sup> = A<sup>M</sup>*A<sup>N</sup>

Первая позволяет быстро вычислить степень числа А, возводя в квадрат это же число в исходной степени;
вторая помогает комбинировать степени любым удобным образом.

Реализация алгоритма:

```scala
def power(a: Long, n: Long): BigInt =
  val bin        = n.toBinaryString.reverse
  val powerArray = new Array[Long](bin.length)
  powerArray(0) = a
  (1 until powerArray.length).foreach(i => powerArray(i) = powerArray(i - 1) * powerArray(i - 1))
  powerArray.indices.foldLeft(BigInt(1)) { (acc, i) =>
    if bin(i) == '1' then acc * powerArray(i) else acc
  }
```


## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Ffundamental%2FNumerical.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Ffundamental%2FNumericalSuite.scala)


---

## References

- [Алгоритм Евклида](https://ru.wikipedia.org/wiki/%D0%90%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC_%D0%95%D0%B2%D0%BA%D0%BB%D0%B8%D0%B4%D0%B0)
- [Род Стивенс - Алгоритмы. Теория и практическое применение. Глава 2. Численные алгоритмы](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
