---
layout: puzzlers
title: "Map Comprehension"
section: puzzlers
prev: captured-by-closures
next: init-you-init-me
---

## {{page.title}}

```scala mdoc:silent
val xs = Seq(Seq("a", "b", "c"), Seq("d", "e", "f"), Seq("g", "h"), Seq("i", "j", "k"))
```

```scala mdoc
val ys = for Seq(x, y, z) <- xs yield x + y + z
```

```scala mdoc:crash
val zs = xs map { case Seq(x, y, z) => x + y + z }
```

Большинство людей думают, что выражение `for-yield-expression` напрямую транслируется в эквивалентный вызов `map`, 
но это правильно только в том случае, если не используется сопоставление с образцом! 
Если используется сопоставление с образцом, также применяется фильтр. 
Приведенный выше пример эквивалентен следующему коду: 

```scala mdoc
xs collect { case Seq(x, y, z) => x + y + z }
```


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-009)
