---
layout: puzzlers
title: "The Devil is in the Defaults"
section: puzzlers
prev: accepts-any-args
next: one-two-skip-a-few
---

## {{page.title}}

```scala mdoc
import collection.mutable

val goodies: Map[String, mutable.Queue[String]] =
  Map().withDefault(_ => mutable.Queue("No superheros here. Keep looking."))
val baddies: Map[String, mutable.Queue[String]] = Map().withDefaultValue(mutable.Queue("No monsters here. Lucky you."))

println(goodies("kitchen").dequeue)
println(baddies("in attic").dequeue)
println(goodies("dining room").dequeue)
```

```scala mdoc:crash
println(baddies("under bed").dequeue)
```

Увидев `withDefault`, может возникнуть соблазн подумать, 
что `withDefaultValue` — это «фабрика», которая каким-то образом создает новый экземпляр 
заданного значения по умолчанию при каждом вызове. 
То, что на самом деле делает `withDefaultValue`, в значительной степени совпадает с тем, 
что он говорит: он возвращает значение, которое ему было дано каждый раз. 
Следовательно, если значение по умолчанию является изменяемым, 
любые внесенные в него изменения повлияют на всех будущих вызывающих объектов. 
Проще придерживаться `withDefault` всякий раз, когда требуется новое значение — изменяемое или неизменное, 
и использовать `withDefaultValue` только тогда, когда каждый раз требуется один и тот же экземпляр.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-042)
