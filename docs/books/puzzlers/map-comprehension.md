# Map Comprehension

```scala
val xs = Seq(Seq("a", "b", "c"), Seq("d", "e", "f"), Seq("g", "h"), Seq("i", "j", "k"))
```

```scala
val ys = for Seq(x, y, z) <- xs yield x + y + z
// ys: Seq[String] = List("abc", "def", "ijk")
```

```scala
val zs = xs map { case Seq(x, y, z) => x + y + z }
// scala.MatchError: List(g, h) (of class scala.collection.immutable.$colon$colon)
```

Большинство людей думают, что выражение `for-yield-expression` напрямую транслируется в эквивалентный вызов `map`, 
но это правильно только в том случае, если не используется сопоставление с образцом! 
Если используется сопоставление с образцом, также применяется фильтр. 
Приведенный выше пример эквивалентен следующему коду: 

```scala
xs collect { case Seq(x, y, z) => x + y + z }
// res0: Seq[String] = List("abc", "def", "ijk")
```


---

**Ссылки:**

- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-009)
