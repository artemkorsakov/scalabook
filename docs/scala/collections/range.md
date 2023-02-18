# Диапазон (Range)

[`Range`](https://scala-lang.org/api/3.x/scala/collection/immutable/Range.html) 
часто используется для заполнения структур данных и для циклов `for`.
Эти примеры демонстрируют, как создавать диапазоны:

```scala
1 to 5
// res0: Inclusive = Range(1, 2, 3, 4, 5)
1 until 5
// res1: Range = Range(1, 2, 3, 4)
1 to 10 by 2
// res2: Range = Range(1, 3, 5, 7, 9)
'a' to 'c'
// res3: Inclusive[Char] = NumericRange('a', 'b', 'c')
```

`Range` можно использовать для заполнения коллекций:

```scala
val x = (1 to 5).toList
// x: List[Int] = List(1, 2, 3, 4, 5)
val y = (1 to 5).toBuffer
// y: Buffer[Int] = ArrayBuffer(1, 2, 3, 4, 5)
```

Они также используются в циклах `for`:

```scala
for i <- 1 to 3 do println(i)
// 1
// 2
// 3
```

Во многих коллекциях есть метод `range`:

```scala
Vector.range(1, 5)
// res5: Vector[Int] = Vector(1, 2, 3, 4)
List.range(1, 10, 2)
// res6: List[Int] = List(1, 3, 5, 7, 9)
Set.range(1, 10)
// res7: Set[Int] = HashSet(5, 1, 6, 9, 2, 7, 3, 8, 4)
```

Диапазоны также полезны для создания тестовых коллекций:

```scala
val evens = (0 to 10 by 2).toList
// evens: List[Int] = List(0, 2, 4, 6, 8, 10)
val odds = (1 to 10 by 2).toList
// odds: List[Int] = List(1, 3, 5, 7, 9)
val doubles = (1 to 5).map(_ * 2.0)
// doubles: IndexedSeq[Double] = Vector(2.0, 4.0, 6.0, 8.0, 10.0)
val map = (1 to 3).map(e => (e, e.toString)).toMap
// map: Map[Int, String] = Map(1 -> "1", 2 -> "2", 3 -> "3")
```


---

**Ссылки:**
- [Scala3 book](https://docs.scala-lang.org/scala3/book/taste-collections.html)
- [Scala3 book, Collections Types](https://docs.scala-lang.org/scala3/book/collections-classes.html)
- [Scala, Immutable collections](https://docs.scala-lang.org/ru/overviews/collections-2.13/concrete-immutable-collection-classes.html)
- [Scala, Mutable collections](https://docs.scala-lang.org/ru/overviews/collections-2.13/concrete-mutable-collection-classes.html)
