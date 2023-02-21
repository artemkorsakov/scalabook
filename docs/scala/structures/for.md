# for

### for loops

В самом простом случае цикл `for` в Scala можно использовать для перебора элементов в коллекции.
Например, имея последовательность целых чисел, можно перебрать ее элементы и вывести значения следующим образом:

```scala
val ints = List(1, 2, 3, 4, 5)
for i <- ints do println(i)
// 1
// 2
// 3
// 4
// 5
```

Код `i <- ints` называется генератором.

Если необходим многострочный блок кода после генератора `for`, используется следующий синтаксис:

```scala
for
  i <- ints
do
  val x = i * 2
  println(s"i = $i, x = $x")
// i = 1, x = 2
// i = 2, x = 4
// i = 3, x = 6
// i = 4, x = 8
// i = 5, x = 10
```

#### Несколько генераторов

В цикле `for` можно использовать несколько генераторов, например:

```scala
for
  i <- 1 to 2
  j <- 'a' to 'b'
  k <- 1 to 10 by 5
do
  println(s"i = $i, j = $j, k = $k")
// i = 1, j = a, k = 1
// i = 1, j = a, k = 6
// i = 1, j = b, k = 1
// i = 1, j = b, k = 6
// i = 2, j = a, k = 1
// i = 2, j = a, k = 6
// i = 2, j = b, k = 1
// i = 2, j = b, k = 6
```

#### Guards

Циклы `for` также могут содержать условия, называемые _guards_:

```scala
for
  i <- 1 to 5
  if i % 2 == 0
do
  println(i)
// 2
// 4
```

Можно добавлять столько условий, сколько необходимо:

```scala
for
  i <- 1 to 10
  if i > 3
  if i < 6
  if i % 2 == 0
do
  println(i)
// 4
```

#### Использование for с Map-ами

Циклы `for` можно использовать с `Map`-ами.
Например, если есть карта ключ/значение:

```scala
val states = Map(
  "AK" -> "Alaska",
  "AL" -> "Alabama", 
  "AR" -> "Arizona"
)
```

Можно обойти все пары ключ/значение так:

```scala
for (abbrev, fullName) <- states do println(s"$abbrev: $fullName")
// AK: Alaska
// AL: Alabama
// AR: Arizona
```

Когда цикл `for` перебирает `Map`, каждая пара ключ/значение привязывается к переменным `abbrev` и `fullName`.
По мере выполнения цикла переменная `abbrev` принимает значение текущего ключа,
а переменная `fullName` - соответствующему ключу значению.

### for expressions

В предыдущих примерах все циклы `for` использовались для побочных эффектов,
в частности, для вывода результата в STDOUT с помощью `println`.

Важно знать, что `for` также можно использовать для выражений, возвращающих значения.
Для этого `for` создается с ключевым словом `yield` вместо `do` и возвращаемым выражением, например:

```scala
val list =
  for
    i <- 10 to 12
  yield
    i * 2
// list: IndexedSeq[Int] = Vector(20, 22, 24)
```

После присваивания `list` содержит `Vector` с отображаемыми значениями. Вот как работает это выражение:
- Выражение `for` начинает перебирать значения в диапазоне `(10, 11, 12)`. Сначала оно работает со значением `10`,
  умножает его на `2`, затем выдает результат - `20`.
- Далее берет `11` — второе значение в диапазоне. Умножает его на `2`,
  а затем выдает значение `22`. Можно представить эти полученные значения как накопление во временном хранилище.
- Наконец, цикл берет число `12` из диапазона, умножает его на `2`, получая число `24`.
  Цикл завершается в этой точке и выдает конечный результат - `(20, 22, 24)`.

В данном случае показанное выражение `for` эквивалентно вызову метода `map`:

```scala
val list = (10 to 12).map(i => i * 2)
// list: IndexedSeq[Int] = Vector(20, 22, 24)
```

Выражения `for` можно использовать всегда, когда нужно обойти все элементы в коллекции
и применить алгоритм к этим элементам для создания нового списка.

Вот пример, который показывает, как использовать блок кода после `yield`:

```scala
val names = List("_olivia", "_walter", "_peter")
// names: List[String] = List("_olivia", "_walter", "_peter")
val capNames = for name <- names yield
  val nameWithoutUnderscore = name.drop(1)
  val capName = nameWithoutUnderscore.capitalize
  capName
// capNames: List[String] = List("Olivia", "Walter", "Peter")
```

Поскольку выражение `for` возвращает результат, его можно использовать в качестве тела метода.
Пример:

```scala
def between3and10(xs: List[Int]): List[Int] =
  for
    x <- xs
    if x >= 3
    if x <= 10
  yield x
between3and10(List(1, 3, 7, 11)) 
// res8: List[Int] = List(3, 7)
```


---

**Ссылки:**

- [Scala3 book, taste Control Structures](https://docs.scala-lang.org/scala3/book/taste-control-structures.html)
- [Scala3 book, Control Structures](https://docs.scala-lang.org/scala3/book/control-structures.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/changed-features/match-syntax.html)
