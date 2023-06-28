# ArrayBuffer

`ArrayBuffer` используется тогда, когда нужна изменяемая индексированная последовательность общего назначения.
Поскольку `ArrayBuffer` индексирован, произвольный доступ к элементам выполняется быстро.

#### Создание ArrayBuffer

Чтобы использовать `ArrayBuffer`, в отличие от предыдущих рассмотренных классов, его нужно вначале импортировать:

```dotty
import scala.collection.mutable.ArrayBuffer
```

Если необходимо начать с пустого `ArrayBuffer`, просто укажите его тип:

```dotty
var strings = ArrayBuffer[String]()
var ints = ArrayBuffer[Int]()
var people = ArrayBuffer[Person]()
```

Если известен примерный размер `ArrayBuffer`, его можно задать:

```dotty
val buf = new ArrayBuffer[Int](100_000)
```

Чтобы создать новый `ArrayBuffer` с начальными элементами, достаточно просто указать начальные элементы,
как для `List` или `Vector`:

```dotty
val nums = ArrayBuffer(1, 2, 3)
val people = ArrayBuffer(
  Person("Bert"),
  Person("Ernie"),
  Person("Grover")
)
```

#### Добавление элементов в ArrayBuffer

Новые элементы добавляются в `ArrayBuffer` с помощью методов `+=` и `++=`.
Также можно использовать текстовый аналог: `append`, `appendAll`, `insert`, `insertAll`, `prepend` и `prependAll`.
Вот несколько примеров с `+=` и `++=`:

```dotty
val nums = ArrayBuffer(1, 2, 3)
// nums: ArrayBuffer[Int] = ArrayBuffer(1, 2, 3)
nums += 4
// res0: ArrayBuffer[Int] = ArrayBuffer(1, 2, 3, 4)
nums ++= List(5, 6)
// res1: ArrayBuffer[Int] = ArrayBuffer(1, 2, 3, 4, 5, 6)
```

#### Удаление элементов из ArrayBuffer

`ArrayBuffer` является изменяемым, поэтому у него есть такие методы, как `-=`, `--=`, `clear`, `remove` и другие.
Примеры с `-=` и `--=`:

```dotty
val a = ArrayBuffer.range('a', 'h')
// a: ArrayBuffer[Char] = ArrayBuffer('a', 'b', 'c', 'd', 'e', 'f', 'g')
a -= 'a'
// res2: ArrayBuffer[Char] = ArrayBuffer('b', 'c', 'd', 'e', 'f', 'g')
a --= Seq('b', 'c')
// res3: ArrayBuffer[Char] = ArrayBuffer('d', 'e', 'f', 'g')
a --= Set('d', 'e')
// res4: ArrayBuffer[Char] = ArrayBuffer('f', 'g')
```

#### Обновление элементов в ArrayBuffer

Элементы в `ArrayBuffer` можно обновлять, либо переназначать:

```dotty
val a = ArrayBuffer.range(1,5)
// a: ArrayBuffer[Int] = ArrayBuffer(1, 2, 3, 4)
a(2) = 50
println(a)
// ArrayBuffer(1, 2, 50, 4)
a.update(0, 10)
println(a)
// ArrayBuffer(10, 2, 50, 4)
```


---

**Ссылки:**

- [Scala3 book](https://docs.scala-lang.org/scala3/book/taste-collections.html)
- [Scala3 book, Collections Types](https://docs.scala-lang.org/scala3/book/collections-classes.html)
- [Scala, Mutable collections](https://docs.scala-lang.org/ru/overviews/collections-2.13/concrete-mutable-collection-classes.html)
