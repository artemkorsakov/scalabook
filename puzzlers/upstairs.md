---
layout: puzzlers
title: "UPSTAIRS downstairs"
section: puzzlers
prev: hi-there
next: location
---

## {{page.title}}

```scala mdoc
val ij: (Int, Int) = (3, 4)
val (i, j): (Int, Int) = (3, 4)
val IJ: (Int, Int) = (3, 4)
```

```scala mdoc:reset:fail
val (I, J): (Int, Int) = (3, 4)
val (`i`, `j`): (Int, Int) = (3, 4)
```

Первые три случая - это присвоение переменным значений кортежа. 
Последние два пытаются сопоставить `(3, 4)` с константами `I` и `J` 
или переменными `i` и `j`, которые должны быть определены ранее — 
Scala полагает, что переменные ВЕРХНЕГО РЕГИСТРА в сопоставлении с образцом являются константами.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-003)
