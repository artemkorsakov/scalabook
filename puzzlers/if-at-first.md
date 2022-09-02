---
layout: puzzlers
title: "If At First..."
section: puzzlers
prev: a-case-of-equality
next: to-map-or-not-to-map
---

## If At First You Don't Succeed...

```scala mdoc
var x = 0
lazy val y = 1 / x

try println(y)
catch
  case _: Throwable =>
    x = 1
    println(y)
```

Одна из самых интересных особенностей ленивых значений (кроме того, что они откладывают фактическое вычисление) 
заключается в том, что они будут пересчитываться при вызове, 
если в момент первого доступа возникло исключение, 
до тех пор, пока не будет получено какое-то определенное значение. 
Таким образом, можно использовать этот полезный шаблон во многих ситуациях, 
например, для обработки отсутствующих файлов.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-012)
