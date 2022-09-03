---
layout: puzzlers
title: "Pick an Int, Any Int!"
section: puzzlers
prev: cast-away
next: a-case-of-string
---

## {{page.title}}

```scala mdoc
class A:
  type X // equivalent to X <: Any
  var x: X = _
  
class B extends A:
  type X = Int
  
val b = new B
println(b.x)
val bX = b.x
println(bX)
```

Поле `x` на уровне байт-кода является объектом (оно было объявлено в `A` и унаследовано). 
`B` специализировал свой тип на `Int`, но используется то же хранилище. 
Это означает, что поведение «неинициализированного `Int`» (в отличие от поведения «неинициализированной ссылки») 
зависит от содержимого поля, распаковываемого в `Int`. 
Распаковка происходит, когда вызывается `b.x`, потому что `x` имеет тип `Int`. 
Этого не происходит, когда выполняется `println(b.x)`, 
потому что `println` принимает аргумент `Any`, поэтому ожидаемый тип выражения — `Any`. 
Scala не понимает необходимости переводить представления, потому что выражение уже преобразовано в `Any`. 

Сравнение:

```scala
scala> println(b.x: Any)
null
scala> println(b.x: Int)
0
```


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-029)