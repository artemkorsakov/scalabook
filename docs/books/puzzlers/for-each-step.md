# For Each Step...

```scala
for
  x <- 1 to 2
  y <- { println("DEBUG 1: x: " + x); x to 1 }
do println(x + y)
// DEBUG 1: x: 1
// 2
// DEBUG 1: x: 2

for
  x <- 1 to 2
  _ = println("DEBUG 2: x: " + x)
  y <- x to 1
do println(x + y)
// DEBUG 2: x: 1
// DEBUG 2: x: 2
// 2
```

Несколько генераторов в `for comprehensions` и `for loops` приводят к вложенным вызовам `flatMap` (SLS §6.19): 

```scala
for {
    a <- expr1
    b <- expr2
    ...
}
``` 

преобразуются в код, эквивалентный: 

```scala
expr1 flatMap { a => expr2 flatMap ... }
``` 

Это означает, что операторы второго генератора – в первом примере `println("DEBUG 1: x: " + x); x to 1` — 
выполняются всякий раз, когда первый генератор переходит к следующему элементу. 
Это происходит только после того, как текущий элемент "полностью" прошел через выражение `for`. 
В случае первого примера это означает, что оператор отладки, когда `x` имеет значение `2`, 
печатается только после того, как оператор `println(x + y)` был выполнен для `x == 1`. 

С другой стороны, определения значений фактически приводят к «преобразованию» генератора, 
с которым они связаны (SLS §6.19): 

```scala
for {
    a <- expr1
    v = vexpr
    b <- expr2
    ...
}
```

преобразуются в код, эквивалентный: 

```scala
expr1 map { a => (a, vexpr) } flatMap { case (a, v) => expr2 flatMap ... }
```

Правостороннее выражение определения значения выполняется для каждого элемента первого генератора до того, 
как элементы генератора будут переданы в оставшуюся часть выражения `for`. 
Во втором примере это означает, что операторы отладки выполняются для каждого элемента выражения 
от `1` до `2` перед первым выполнением (т.е. когда `x` имеет значение `1`) оператора `println(x + y)`.


---

**Ссылки:**

- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-068)
