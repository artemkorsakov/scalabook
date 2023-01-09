# Сортировка

Сортировка — обычная операция как в жизни, так и в информатике.
В нашей повседневной жизни мы сортируем элементы, чтобы поиск был быстрее, это верно и в области компьютерных наук.
В конце концов, компьютеры существуют, чтобы помогать нам в повседневных задачах.
Сортировка может присутствовать на разных уровнях в зависимости от требований. 
Например, иногда нам может понадобиться отсортировать символ за символом. 
В других случаях нам может понадобиться отсортировать более крупные структуры, например профили компаний. 
Какой бы ни была причина, основной принцип сортировки остается прежним. 

Кроме того, существует два порядка сортировки — по возрастанию и по убыванию. 
Предположим, что нужно отсортировать **n** элементов: **<a<sub>1</sub>,a<sub>2</sub>,a<sub>3</sub>,...,a<sub>n</sub>>**. 
Когда эти элементы отсортированы в порядке возрастания, 
их отношение можно формально сформулировать как **a<sub>1</sub> ≤ a<sub>2</sub> ≤ a<sub>3</sub> ≤ ... ≤ a<sub>n</sub>**. 
Точно так же их сортировка по убыванию может быть формально сформулирована 
как **a<sub>n</sub> ≥ a<sub>n−1</sub> ≥ a<sub>n−2</sub> ≥ ... ≥ a<sub>2</sub> ≥ a<sub>1</sub>**.


## Сортировка пузырьком (bubble sort)

Алгоритм [сортировки пузырьком](https://ru.wikipedia.org/wiki/%D0%A1%D0%BE%D1%80%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%BA%D0%B0_%D0%BF%D1%83%D0%B7%D1%8B%D1%80%D1%8C%D0%BA%D0%BE%D0%BC) 
состоит из повторяющихся проходов по сортируемому массиву. 
За каждый проход элементы последовательно сравниваются попарно 
и, если порядок в паре неверный, выполняется перестановка элементов. 
Проходы по массиву повторяются N-1 раз или до тех пор, пока на очередном проходе не окажется, 
что обмены больше не нужны, что означает — массив отсортирован. 
При каждом проходе алгоритма по внутреннему циклу очередной наибольший элемент массива ставится 
на своё место в конце массива рядом с предыдущим «наибольшим элементом», 
а наименьший элемент перемещается на одну позицию к началу массива 
(«всплывает» до нужной позиции, как пузырёк в воде — отсюда и название алгоритма).

Возможная реализация алгоритма такая:

```scala
def bubbleSort[T: Ordering](array: Array[T]): Unit =
  val ord = summon[Ordering[T]]
  def loop(j: Int): Boolean =
    (0 to array.length - 1 - j)
      .withFilter(i => ord.gt(array(i), array(i + 1)))
      .map(i => swap(array, i, i + 1))
      .nonEmpty
  (1 until array.length).takeWhile(loop)
  ()

private def swap[T](array: Array[T], i: Int, j: Int): Unit =
  val temp = array(j)
  array(j) = array(i)
  array(i) = temp
```

## Сортировка выбором (selection sort)

Шаги [алгоритма сортировки выбором](https://ru.wikipedia.org/wiki/%D0%A1%D0%BE%D1%80%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%BA%D0%B0_%D0%B2%D1%8B%D0%B1%D0%BE%D1%80%D0%BE%D0%BC):
- находим номер минимального значения в текущем списке
- производим обмен этого значения со значением первой неотсортированной позиции (обмен не нужен, если минимальный элемент уже находится на данной позиции)
- теперь сортируем хвост списка, исключив из рассмотрения уже отсортированные элементы

Возможная реализация алгоритма такая:

```scala
def selectionSort[T: Ordering](array: Array[T]): Unit =
  (0 until array.length - 1).foreach { i =>
    val j = (i until array.length).minBy(j => array(j))
    swap(array, i, j)
  }
```

## Сортировка вставками (insertion sort)

В начальный момент отсортированная последовательность пуста. 
На каждом шаге [алгоритма сортировки вставками](https://ru.wikipedia.org/wiki/%D0%A1%D0%BE%D1%80%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%BA%D0%B0_%D0%B2%D1%81%D1%82%D0%B0%D0%B2%D0%BA%D0%B0%D0%BC%D0%B8)
выбирается один из элементов входных данных и помещается на нужную позицию в уже отсортированной последовательности до тех пор, 
пока набор входных данных не будет исчерпан. 
В любой момент времени в отсортированной последовательности элементы удовлетворяют требованиям к выходным данным алгоритма.

Возможная реализация алгоритма такая:

```scala
def insertionSort[T: Ordering](array: Array[T]): Unit =
  val ord = summon[Ordering[T]]
  (1 until array.length).foreach { j =>
    val key = array(j)
    var i = j - 1
    while i >= 0 && ord.gt(array(i), key)
    do
      array(i + 1) = array(i)
      i -= 1
    array(i + 1) = key
  }
```

## Сортировка слиянием (merge sort)

[Алгоритм сортировки слиянием](https://ru.wikipedia.org/wiki/%D0%A1%D0%BE%D1%80%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%BA%D0%B0_%D1%81%D0%BB%D0%B8%D1%8F%D0%BD%D0%B8%D0%B5%D0%BC) выглядят так:
- сортируемый массив разбивается на две части примерно одинакового размера
- каждая из получившихся частей сортируется отдельно, например — тем же самым алгоритмом
- два упорядоченных массива половинного размера соединяются в один

Возможная реализация алгоритма такая:

```scala
def mergeSort[T: ClassTag: Ordering](array: Array[T]): Unit =
  mergeSort(array, 0, array.length - 1)

private def mergeSort[T: ClassTag: Ordering](array: Array[T], first: Int, last: Int): Unit =
  if last <= first then ()
  else
    val mid = first + (last - first) / 2
    mergeSort(array, first, mid)
    mergeSort(array, mid + 1, last)
    mergeParts(array, first, last, mid)

private def mergeParts[T: ClassTag: Ordering](array: Array[T], first: Int, last: Int, mid: Int): Unit =
  val buf = new Array[T](array.length)
  array.copyToArray(buf)

  var i = first
  var j = mid + 1
  for (k <- first to last)
    if i > mid then
      array(k) = buf(j)
      j += 1
    else if j > last then
      array(k) = buf(i)
      i += 1
    else if summon[Ordering[T]].lt(buf(j), buf(i)) then
      array(k) = buf(j)
      j += 1
    else
      array(k) = buf(i)
      i += 1
```

## Быстрая сортировка (quicksort)

Общая идея [алгоритма быстрой сортировки](https://ru.wikipedia.org/wiki/%D0%91%D1%8B%D1%81%D1%82%D1%80%D0%B0%D1%8F_%D1%81%D0%BE%D1%80%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%BA%D0%B0) состоит в следующем:
- выбрать из массива элемент, называемый опорным. Это может быть любой из элементов массива. 
  От выбора опорного элемента не зависит корректность алгоритма, но в отдельных случаях может сильно зависеть его эффективность.
- сравнить все остальные элементы с опорным и переставить их в массиве так, чтобы разбить массив на три непрерывных отрезка, 
  следующих друг за другом: «элементы меньшие опорного», «равные» и «большие».
- для отрезков «меньших» и «больших» значений выполнить рекурсивно ту же последовательность операций, если длина отрезка больше единицы

Возможная реализация алгоритма такая:

```scala
def quickSort[T: Ordering](list: List[T]): List[T] =
  list match
    case Nil      => list
    case _ :: Nil => list
    case h :: tail =>
      val (p1, p2) = tail.partition(el => summon[Ordering[T]].lteq(el, h))
      val leftToPivot = quickSort(p1)
      val rightToPivot = quickSort(p2)
      leftToPivot ++ (h :: rightToPivot)
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Fsort%2FSorting.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Fsort%2FSortingSuite.scala)

---

## Ссылки

- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
