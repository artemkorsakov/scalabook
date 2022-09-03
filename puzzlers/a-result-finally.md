---
layout: puzzlers
title: "A Result, Finally!"
section: puzzlers
prev: splitting-headache
next: heads-you-win
---

## {{page.title}}

```scala mdoc
var errCount: Int = 0
def incErrs(): Int = { errCount += 1; errCount }

def tryEval(expr: => Double) =
  var res = Double.NaN
  try res = expr
  catch case _: Exception => incErrs()
  finally res

println(tryEval(10 / 4))
println(tryEval(10 / 0))
```

В Scala, если функция не завершается с помощью явного возвращаемого выражения, 
значение, возвращаемое функцией, является значением последнего выражения в теле функции, которое должно быть выполнено. 
Однако в этом отношении операторы в блоке `finally` не считаются «отдельными» выражениями в теле функции. 
Последнее выражение в теле функции `tryEval` — это полное выражение `try...finally`, а не простое выражение `res`. 
В соответствии с §6.22 Спецификации языка Scala значение, возвращаемое выражением `try...finally`, 
является либо последним успешно вычисленным значением в блоке `try`, 
либо, если возникает исключение, значением, 
возвращаемым первым совпадающим случаем в блоке обработчик исключений. 
В этом случае значения, возвращаемые из `tryEval`, равны `()` (поскольку присваивания в Scala не возвращают значение) 
и `1` (результат увеличения счетчика ошибок в первый раз) соответственно.


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-059)