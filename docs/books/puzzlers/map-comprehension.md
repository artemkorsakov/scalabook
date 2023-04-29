# Map Comprehension

```scala mdoc:silent
val xs = Seq(Seq("a", "b", "c"), Seq("d", "e", "f"), Seq("g", "h"), Seq("i", "j", "k"))
```

```scala mdoc
val ys = for Seq(x, y, z) <- xs yield x + y + z
```

```scala
val zs = xs map { case Seq(x, y, z) => x + y + z }
// scala.MatchError: List(g, h) (of class scala.collection.immutable.$colon$colon)
```

Большинство людей думают, что выражение `for-yield-expression` напрямую транслируется в эквивалентный вызов `map`, 
но это правильно только в том случае, если не используется сопоставление с образцом! 
Если используется сопоставление с образцом, также применяется фильтр. 
Приведенный выше пример эквивалентен следующему коду: 

```scala mdoc
xs collect { case Seq(x, y, z) => x + y + z }
```


---

**Ссылки:**

- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-009)
