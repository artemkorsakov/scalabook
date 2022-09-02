---
layout: puzzlers
title: "Hi There!"
section: puzzlers
prev: index
next: upstairs
---

## {{page.title}}

```scala mdoc
List(1, 2).map { i => i + 1 }
List(1, 2).map { _ + 1 }
List(1, 2).map { i => println("Hi"); i + 1 }
List(1, 2).map { println("Hi"); _ + 1 }
```

Несмотря на то, что упрощение `_` выглядит так же, во втором случае оно имеет совсем другой эффект: 
оператор `println` больше не является частью тела функции! 
Вместо этого выражение оценивается при определении функции, которую необходимо передать `map` — 
как и во всех блоках, возвращается последнее значение.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-001)
