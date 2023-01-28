# Hi There!

```scala
List(1, 2).map { i => i + 1 }
// res0: List[Int] = List(2, 3)
List(1, 2).map { _ + 1 }
// res1: List[Int] = List(2, 3)
List(1, 2).map { i => println("Hi"); i + 1 }
// Hi
// Hi
// res2: List[Int] = List(2, 3)
List(1, 2).map { println("Hi"); _ + 1 }
// Hi
// res3: List[Int] = List(2, 3)
```

Несмотря на то, что упрощение `_` выглядит так же, во втором случае оно имеет совсем другой эффект: 
оператор `println` больше не является частью тела функции! 
Вместо этого выражение оценивается при определении функции, которую необходимо передать `map` — 
как и во всех блоках, возвращается последнее значение.


---

**Ссылки:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-001)
