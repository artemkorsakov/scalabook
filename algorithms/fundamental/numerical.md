# Численные алгоритмы

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


## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Ffundamental%2FNumerical.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Ffundamental%2FNumericalSuite.scala)


---

## References

- [Род Стивенс - Алгоритмы. Теория и практическое применение. Глава 2. Численные алгоритмы](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
