# Maps

`Map` — это итерируемая коллекция, состоящая из пар ключей и значений.
В Scala есть как изменяемые, так и неизменяемые типы `Map`.
В этом разделе показано, как использовать неизменяемый `Map`.

#### Создание Map

Неизменяемая `Map` создается следующим образом:

```scala
val states = Map(
  "AK" -> "Alaska",
  "AL" -> "Alabama",
  "AZ" -> "Arizona"
)
```

Перемещаться по элементам `Map` в цикле `for` можно следующим образом:

```scala
for (k, v) <- states do println(s"key: $k, value: $v")
// key: AK, value: Alaska
// key: AL, value: Alabama
// key: AZ, value: Arizona
```

#### Доступ к элементам Map

Доступ к элементам `Map` осуществляется через указание в скобках значения ключа:

```scala
val ak = states("AK")
// ak: String = "Alaska"
val al = states("AL")
// al: String = "Alabama"
```

На практике также используются такие методы, как `keys`, `keySet`, `keysIterator`, циклы `for`
и функции высшего порядка, такие как `map`, для работы с ключами и значениями `Map`.

#### Добавление элемента в Map

При добавлении элементов в неизменяемую карту с помощью `+` и `++`, создается новая карта:

```scala
val a = Map(1 -> "one")
// a: Map[Int, String] = Map(1 -> "one")
val b = a + (2 -> "two")
// b: Map[Int, String] = Map(1 -> "one", 2 -> "two")
val c = b ++ Seq(
  3 -> "three",
  4 -> "four"
)
// c: Map[Int, String] = Map(1 -> "one", 2 -> "two", 3 -> "three", 4 -> "four")
```

#### Удаление элементов из Map

Элементы удаляются с помощью методов `-` или `--`.
В случае неизменяемой `Map` создается новый экземпляр, который нужно присвоить новой переменной:

```scala
val a = Map(
  1 -> "one",
  2 -> "two",
  3 -> "three",
  4 -> "four"
)
// a: Map[Int, String] = Map(1 -> "one", 2 -> "two", 3 -> "three", 4 -> "four")
val b = a - 4    
// b: Map[Int, String] = Map(1 -> "one", 2 -> "two", 3 -> "three")    
val c = a - 4 - 3
// c: Map[Int, String] = Map(1 -> "one", 2 -> "two")
```

#### Обновление элементов в Map

Чтобы обновить элементы на неизменяемой `Map`, используется метод `update` (или оператор `+`):

```scala
val a = Map(
  1 -> "one",
  2 -> "two",
  3 -> "three"
)
// a: Map[Int, String] = Map(1 -> "one", 2 -> "two", 3 -> "three")
val b = a.updated(3, "THREE!")
// b: Map[Int, String] = Map(1 -> "one", 2 -> "two", 3 -> "THREE!")
val c = a + (2 -> "TWO...")
// c: Map[Int, String] = Map(1 -> "one", 2 -> "TWO...", 3 -> "three")
```

#### Перебор элементов в Map

Элементы в `Map` можно перебрать с помощью цикла `for`, как и для остальных коллекций:

```scala
val states = Map(
  "AK" -> "Alaska",
  "AL" -> "Alabama",
  "AZ" -> "Arizona"
)
for (k, v) <- states do println(s"key: $k, value: $v")
// key: AK, value: Alaska
// key: AL, value: Alabama
// key: AZ, value: Arizona
```

Существует много способов работы с ключами и значениями на `Map`.
Общие методы `Map` включают `foreach`, `map`, `keys` и `values`.

В Scala есть много других специализированных типов `Map`,
включая `CollisionProofHashMap`, `HashMap`, `LinkedHashMap`, `ListMap`, `SortedMap`, `TreeMap`, `WeakHashMap` и другие.


---

**Ссылки:**
- [Scala3 book](https://docs.scala-lang.org/scala3/book/taste-collections.html)
- [Scala3 book, Collections Types](https://docs.scala-lang.org/scala3/book/collections-classes.html)
- [Scala, Immutable collections](https://docs.scala-lang.org/ru/overviews/collections-2.13/concrete-immutable-collection-classes.html)
- [Scala, Mutable collections](https://docs.scala-lang.org/ru/overviews/collections-2.13/concrete-mutable-collection-classes.html)
