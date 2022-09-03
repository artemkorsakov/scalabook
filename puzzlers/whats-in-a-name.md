---
layout: puzzlers
title: "What's in a Name?"
section: puzzlers
prev: count-me-now-count-me-later
next: i-can-has-padding
---

## {{page.title}}

```scala mdoc
class C:
  def sum(x: Int = 1, y: Int = 2): Int = x + y
  
class D extends C:
  override def sum(y: Int = 3, x: Int = 4): Int = super.sum(x, y)

val d: D = new D
val c: C = d
c.sum(x = 0)
d.sum(x = 0)
```

Scala использует статический тип переменной для привязки имен параметров, 
но значения по умолчанию определяются типом времени выполнения: 
- Привязка имен параметров выполняется компилятором, и единственная информация, 
которую может использовать компилятор, — это статический тип переменной. 
- Для параметров со значением по умолчанию компилятор создает методы, 
вычисляющие выражения аргументов по умолчанию (SLS §4.6). 
В приведенном выше примере оба класса `C` и `D` содержат методы `sum$default$1` и `sum$default$2` 
для двух параметров по умолчанию. Когда параметр отсутствует, компилятор использует результат этих методов, 
и эти методы вызываются во время выполнения.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-024)