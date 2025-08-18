{%
showPrev = true
prevPage = reduction.html
showNext = true
nextPage = higher_order_function.html
%}

# Хвостовая рекурсия

## Введение в рекурсию

Рекурсия в программировании — это техника, при которой функция вызывает сама себя для решения задачи. 
В Scala и других функциональных языках рекурсия играет ключевую роль, 
так как часто заменяет итеративные конструкции (циклы) из императивных языков.

Существует два основных типа рекурсии:

1. **Головная рекурсия (Head recursion)** — рекурсивный вызов выполняется не в последней операции, 
   а затем с результатом производятся дополнительные вычисления.
2. **Хвостовая рекурсия (Tail recursion)** — рекурсивный вызов является последней операцией в функции, 
   без последующих вычислений с его результатом.

### Проблема головной рекурсии

Главная проблема головной рекурсии — потенциальное переполнение стека вызовов. 
Каждый рекурсивный вызов добавляет новый кадр в стек, 
и при большой глубине рекурсии это может привести к ошибке `StackOverflowError`.

**Пример головной рекурсии:**

```dotty
def factorial(n: Int): Int =
  if n <= 1 then 1
  // Не хвостовая рекурсия - умножение происходит после рекурсивного вызова
  else n * factorial(n - 1)
```

## Хвостовая рекурсия в деталях

### Принцип работы

Хвостовая рекурсия позволяет компилятору Scala выполнить **оптимизацию хвостового вызова (Tail Call Optimization — TCO)**. 
Вместо добавления нового кадра стека для каждого рекурсивного вызова, 
компилятор может переиспользовать текущий кадр, эффективно превращая рекурсию в цикл.

**Ключевые характеристики хвостовой рекурсии:**

1. Рекурсивный вызов должен быть последней операцией в функции
2. Часто используется аккумулятор для накопления результата
3. Требует аннотации `@tailrec` для проверки компилятором

### Пример с вычислением длины списка

Рассмотрим более детально пример с вычислением длины списка.

#### Головная рекурсия (проблемная версия)

```dotty
def recursiveLength(list: List[String]): Long =
  list match
    case Nil       => 0
    case _ :: tail => 1 + recursiveLength(tail)
    
recursiveLength((1 to 100000).map(_.toString).toList)
// java.lang.StackOverflowError
//   at scala.collection.immutable.List.equals(List.scala:613)
//   at rs$line$1$.recursiveLength(rs$line$1:3)
//   at rs$line$1$.recursiveLength(rs$line$1:4)
//   at rs$line$1$.recursiveLength(rs$line$1:4)
//   ...
```

**Проблемы:**

1. После рекурсивного вызова `recursiveLength(tail)` выполняется операция сложения (`1 + ...`)
2. Каждый вызов сохраняет в стеке информацию о том, что нужно вернуться и добавить `1`
3. Для большого списка (например, 100 000 элементов) это вызовет переполнение стека

#### Хвостовая рекурсия (оптимизированная версия)

```dotty
import scala.annotation.tailrec

@tailrec
def tailRecursiveLength(list: List[String], accumulator: Long = 0): Long =
  list match
    case Nil       => accumulator
    case _ :: tail => tailRecursiveLength(tail, accumulator + 1)

tailRecursiveLength((1 to 100000).map(_.toString).toList)
// val res0: Long = 100000
```

**Преимущества:**

1. Рекурсивный вызов — последняя операция
2. Аккумулятор (`accumulator`) хранит промежуточный результат
3. Компилятор может оптимизировать это в эквивалент цикла
4. Работает для сколь угодно больших списков без переполнения стека

### Как работает оптимизация

Рассмотрим, как выполняется каждая версия для списка `List("a", "b", "c")`:

**Головная рекурсия:**

```
recursiveLength(["a", "b", "c"])
1 + recursiveLength(["b", "c"])
1 + (1 + recursiveLength(["c"]))
1 + (1 + (1 + recursiveLength([])))
1 + (1 + (1 + 0)))
1 + (1 + 1)
1 + 2
3
```

Каждый шаг требует сохранения контекста для последующего сложения.

**Хвостовая рекурсия:**

```
tailRecursiveLength(["a", "b", "c"], 0)
tailRecursiveLength(["b", "c"], 1)
tailRecursiveLength(["c"], 2)
tailRecursiveLength([], 3)
3
```

Здесь каждый шаг полностью завершается перед следующим вызовом, позволяя переиспользовать стековый кадр.

## Практические примеры

### Пример 1: Факториал

**Головная рекурсия:**

```dotty
def factorial(n: Int): Int =
  if n <= 1 then 1
  // Умножение после рекурсивного вызова
  else n * factorial(n - 1)
```

**Хвостовая рекурсия:**

```dotty
@tailrec
def factorialTailRec(n: Int, accumulator: Int = 1): Int =
  if n <= 1 then accumulator
  // Все вычисления перед вызовом
  else factorialTailRec(n - 1, n * accumulator)
```

### Пример 2: Числа Фибоначчи

**Головная рекурсия (неэффективная):**

```dotty
def fib(n: Int): Int =
  if n <= 1 then n
  // Два рекурсивных вызова + сложение
  else fib(n - 1) + fib(n - 2)
```

**Хвостовая рекурсия (эффективная):**

```dotty
@tailrec
def fibTailRec(n: Int, a: Int = 0, b: Int = 1): Int =
  if n == 0 then a
  // Хвостовой вызов с передачей состояния
  else fibTailRec(n - 1, b, a + b)
```

## Работа с компилятором

### Аннотация @tailrec

Аннотация `@tailrec` — это контракт с компилятором:

1. Компилятор проверяет, действительно ли рекурсия хвостовая
2. Если нет — выдает ошибку компиляции
3. Если да — применяет оптимизацию

**Пример ошибочного использования:**

```dotty
@tailrec // Вызовет ошибку компиляции
def sum(list: List[Int]): Int =
  list match
    case Nil    => 0
    case h :: t => h + sum(t) // Сложение после рекурсивного вызова

// -- Error: ----------------------------------------------------------------------
// 5 |    case h :: t => h + sum(t)
//   |                       ^^^^^^
//   |                 Cannot rewrite recursive call: it is not in tail position
// 1 error found
```

### Типичные ошибки при попытке использовать хвостовую рекурсию

1. **Вычисления после рекурсивного вызова:**
   ```dotty
   @tailrec
   def foo(n: Int): Int =
     if n == 0 then 0
     else foo(n - 1) + 1 // Ошибка: сложение после вызова
   ```

2. **Не прямой хвостовой вызов:**
   ```dotty
   @tailrec
   def bar(n: Int): Int =
     if n == 0 then 0
     else bar(bar(n - 1))  // Ошибка: вложенный вызов
   ```

3. **Отсутствие базового случая:**
   ```dotty
   @tailrec
   def infinite(n: Int): Int = infinite(n + 1)  // Технически хвостовая, но бесконечная
   ```

## Продвинутые техники

### Трансформация в хвостовую рекурсию

Не все алгоритмы очевидно выражаются через хвостовую рекурсию. 
Иногда требуется рефакторинг:

**Пример: обход дерева**

```dotty
sealed trait Tree
case class Node(value: Int, left: Tree, right: Tree) extends Tree
case object Leaf                                     extends Tree

// Головная рекурсия
def sumTree(tree: Tree): Int = tree match
  case Leaf          => 0
  case Node(v, l, r) => v + sumTree(l) + sumTree(r)

// Хвостовая рекурсия с использованием явного стека
def sumTreeTailRec(tree: Tree): Int =
  @tailrec
  def loop(remaining: List[Tree], acc: Int): Int = remaining match
    case Nil                   => acc
    case Leaf :: tail          => loop(tail, acc)
    case Node(v, l, r) :: tail => loop(l :: r :: tail, acc + v)

  loop(List(tree), 0)
```

## Производительность

### Сравнение с итеративными решениями

После оптимизации хвостовая рекурсия по производительности идентична итеративным решениям:

```dotty
// Итеративная версия
def lengthIterative(list: List[String]): Long =
  var acc     = 0L
  var current = list
  while current.nonEmpty do
    acc += 1
    current = current.tail
  acc

// Хвостовая рекурсия (после компиляции будет аналогична итеративной)
@tailrec
def lengthTailRec(list: List[String], acc: Long = 0): Long =
  list match
    case Nil       => acc
    case _ :: tail => lengthTailRec(tail, acc + 1)
```

### Ограничения JVM

На JVM есть некоторые ограничения для TCO:
полная оптимизация работает только для прямых хвостовых вызовов в одном методе

## Заключение

Хвостовая рекурсия — мощный инструмент в Scala, который:

1. Позволяет избежать переполнения стека
2. Сохраняет декларативный стиль функционального программирования
3. Обеспечивает производительность, сравнимую с итеративными решениями

**Рекомендации по использованию:**

1. Всегда используйте `@tailrec` для публичных рекурсивных методов
2. Преобразуйте головную рекурсию в хвостовую с помощью аккумуляторов
3. Для сложных структур данных (деревья, графы) используйте явное управление состоянием (стек/очередь)
4. Проверяйте производительность в пограничных случаях

Хвостовая рекурсия — это не только оптимизация, 
но и способ мышления о рекурсивных алгоритмах, где состояние явно передается через параметры, 
а не фоново через стек вызовов.

## Упражнения

### Упражнение 1.4.1: Поиск максимального элемента в списке

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

**Задача:** 

Написать хвостовую рекурсивную функцию, находящую максимальный элемент в списке.

```dotty
def maxTailRec(list: List[Int]): Option[Int] =
  @annotation.tailrec
  def loop(???): Int =
    ???

  ???
```

**Примеры:**

```dotty
maxTailRec(List.empty)          // None
maxTailRec(List(1))             // Some(1)
maxTailRec(List(1, 2, 3, 4, 5)) // Some(5)
```

@:note(🔍 Показать решение)

Функция `maxTailRec` находит максимальный элемент в списке с использованием хвостовой рекурсии: 
внутренняя функция `loop` последовательно сравнивает элементы списка с текущим максимумом (`currentMax`), 
обновляя его при нахождении большего значения, 
а внешняя функция обрабатывает пустой список (возвращая `None`) 
и запускает рекурсию с первым элементом в качестве начального максимума (возвращая результат в `Some`). 
Аннотация `@tailrec` гарантирует, что рекурсия будет оптимизирована в цикл.

```dotty
def maxTailRec(list: List[Int]): Option[Int] =
  @annotation.tailrec
  def loop(
      remaining: List[Int],
      currentMax: Int
  ): Int =
    remaining match
      case Nil => currentMax
      case head :: tail =>
        val newMax =
          if head > currentMax then head
          else currentMax
        loop(tail, newMax)

  if list.isEmpty then None
  else Some(loop(list.tail, list.head))
```

@:@

### Упражнение 1.4.2: Подсчёт вхождений элемента в список

**Задача**:

Написать хвостовую рекурсивную функцию, которая считает, сколько раз элемент встречается в списке.

```dotty
def countOccurrences[A](list: List[A], target: A): Int =
  ???
```

**Примеры:**

```dotty
countOccurrences(List.empty, 1)             // 0
countOccurrences(List(1, 2, 3), 1)          // 1
countOccurrences(List(1, 2, 3, 2, 4, 2), 2) // 3
```

@:note(🔍 Показать решение)

Функция `countOccurrences` подсчитывает количество вхождений элемента `target` 
в список `list` с помощью хвостовой рекурсии: 
внутренняя функция `loop` проходит по списку, увеличивая аккумулятор `acc` на 1 при совпадении элемента с `target`, 
и возвращает итоговое значение аккумулятора после обработки всего списка. 
Аннотация `@tailrec` обеспечивает оптимизацию рекурсии в цикл.

```dotty
def countOccurrences[A](
    list: List[A],
    target: A
): Int =
  @annotation.tailrec
  def loop(remaining: List[A], acc: Int): Int =
    remaining match
      case Nil => acc
      case head :: tail =>
        val newAcc =
          if head == target then acc + 1
          else acc
        loop(tail, newAcc)

  loop(list, 0)
```

@:@

### Упражнение 1.4.3: Проверка, является ли число простым

**Задача:** 

Написать хвостовую рекурсивную функцию, проверяющую, является ли число простым.

**Подсказка:**

- Простое число делится только на `1` и само себя.
- Можно проверять делители от `2` до `sqrt(n)`.

```dotty
def isPrime(n: Int): Boolean =
  ???
```

**Примеры:**

```dotty
isPrime(11) // true
isPrime(12) // false
```

@:note(🔍 Показать решение)

Функция `isPrime` проверяет, является ли число `n` простым, используя хвостовую рекурсию: 
внутренняя функция `checkDivisor` последовательно проверяет делители от `2` до квадратного корня из `n`, 
возвращая `false` при обнаружении делителя или `true`, если делителей не найдено. 
Для чисел ≤ 1 сразу возвращается `false`, 
а аннотация `@tailrec` гарантирует оптимизацию рекурсивных вызовов в цикл.

```dotty
def isPrime(n: Int): Boolean =
  @annotation.tailrec
  def checkDivisor(
      current: Int,
      maxDivisor: Int
  ): Boolean =
    if current > maxDivisor then true
    else if n % current == 0 then false
    else checkDivisor(current + 1, maxDivisor)

  if n <= 1 then false
  else checkDivisor(2, math.sqrt(n).toInt)
```

@:@

### Упражнение 1.4.4: Разворот списка

**Задача:** Написать хвостовую рекурсивную функцию, которая разворачивает список.

**Обычная рекурсия:**

```dotty
def reverse[A](list: List[A]): List[A] =
  list match
    case Nil          => Nil
    case head :: tail => reverse(tail) :+ head
```

**Проблема:** `:+` (добавление в конец) неэффективно для списков.

**Задание:** Переписать с хвостовой рекурсией и аккумулятором.

```dotty
def reverseTailRec[A](list: List[A]): List[A] =
  ???
```

**Примеры:**

```dotty
reverseTailRec(List.empty)          // List()
reverseTailRec(List(1))             // List(1)
reverseTailRec(List(1, 2, 3, 4, 5)) // List(5, 4, 3, 2, 1)
```

@:note(🔍 Показать решение)

Функция `reverseTailRec` переворачивает список с использованием хвостовой рекурсии: 
внутренняя функция `loop` последовательно перемещает элементы из исходного списка (`remaining`) в аккумулятор (`acc`), 
добавляя каждый текущий элемент в голову аккумулятора, что в итоге даёт обратный порядок элементов. 
Аннотация `@tailrec` гарантирует оптимизацию рекурсии в цикл, 
а начальный вызов `loop(list, Nil)` запускает процесс с пустым аккумулятором.

```dotty
def reverseTailRec[A](list: List[A]): List[A] =
  @annotation.tailrec
  def loop(remaining: List[A], acc: List[A]): List[A] =
    remaining match
      case Nil          => acc
      case head :: tail => loop(tail, head :: acc)

  loop(list, Nil)
```

@:@

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Харрисон - Введение в функциональное программирование](https://funprog-ru.github.io/)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)
- [Tail Recursion in Scala - Baeldung](https://www.baeldung.com/scala/tail-recursion)

[sample]: https://gitverse.ru/scalabook/fp
