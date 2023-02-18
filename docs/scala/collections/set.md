# Работа с множествами

Множество ([Set](https://scala-lang.org/api/3.x/scala/collection/immutable/Set.html)) -
итерируемая коллекция без повторяющихся элементов.

В Scala есть как изменяемые, так и неизменяемые типы `Set`.
В этом разделе демонстрируется неизменяемое множество.

#### Создание множества

Создание нового пустого множества:

```scala
val nums = Set[Int]()
// nums: Set[Int] = Set()
val letters = Set[Char]()
// letters: Set[Char] = Set()
```

Создание множества с исходными данными:

```scala
val nums = Set(1, 2, 3, 3, 3)
// nums: Set[Int] = Set(1, 2, 3)
val letters = Set('a', 'b', 'c', 'c')
// letters: Set[Char] = Set('a', 'b', 'c')
```

#### Добавление элементов в множество

В неизменяемое множество новые элементы добавляются с помощью `+` и `++`,
результат присваивается новой переменной:

```scala
val a = Set(1, 2)
// a: Set[Int] = Set(1, 2)
val b = a + 3
// b: Set[Int] = Set(1, 2, 3)
val c = b ++ Seq(4, 1, 5, 5)
// c: Set[Int] = HashSet(5, 1, 2, 3, 4)
```

Стоит отметить, что повторяющиеся элементы не добавляются в множество, а также, что порядок элементов произвольный.

#### Удаление элементов из множества

Элементы из множества удаляются с помощью методов `-` и `--`:

```scala
val a = Set(1, 2, 3, 4, 5)
// a: Set[Int] = HashSet(5, 1, 2, 3, 4)
val b = a - 5
// b: Set[Int] = HashSet(1, 2, 3, 4)
val c = b -- Seq(3, 4)
// c: Set[Int] = HashSet(1, 2)
```


---

**Ссылки:**
- [Scala3 book](https://docs.scala-lang.org/scala3/book/taste-collections.html)
- [Scala3 book, Collections Types](https://docs.scala-lang.org/scala3/book/collections-classes.html)
- [Scala, Immutable collections](https://docs.scala-lang.org/ru/overviews/collections-2.13/concrete-immutable-collection-classes.html)
- [Scala, Mutable collections](https://docs.scala-lang.org/ru/overviews/collections-2.13/concrete-mutable-collection-classes.html)
