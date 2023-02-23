# if/else

Однострочное `if` выражение выглядит так:

```scala
val x = 1
if x == 1 then println(x)
// 1
```

Когда необходимо выполнить несколько строк кода после `if`, используется синтаксис:

```scala
if x == 1 then
  println("x is 1, as you can see:")
  println(x)
// x is 1, as you can see:
// 1
```

`if`/`else` синтаксис выглядит так:

```scala
if x == 1 then
  println("x is 1, as you can see:")
  println(x)
else
  println("x was not 1")
// x is 1, as you can see:
// 1
```

`if`/`else if`/`else` выглядит так же, как и в других языках:

```scala
def detect(x: Int) = 
  if x < 0 then
    println("negative")
  else if x == 0 then
    println("zero")
  else
    println("positive")
detect(-1)    
// negative    
detect(0) 
// zero 
detect(1) 
// positive 
```

При желании можно дополнительно включить оператор `end if` в конце каждого выражения:

```scala
if x == 1 then
  println("x is 1, as you can see:")
  println(x)
end if
```

#### if/else выражение всегда возвращает результат

Сравнения `if/else` образуют выражения - это означает, что они возвращают значение,  которое можно присвоить переменной.
Поэтому нет необходимости в специальном тернарном операторе.
Пример:

```scala
val minValue = if a < b then a else b
```

Можно использовать `if/else` выражение в качестве тела метода:

```scala
def compare(a: Int, b: Int): Int =
  if a < b then -1
  else if a == b then 0
  else 1
```

Как будет видно дальше, все структуры управления Scala можно использовать в качестве выражений.

> Программирование, ориентированное на выражения (_expression-oriented programming_ или _EOP_) -
> стиль разработки, когда каждое написанное выражение возвращает значение.
>
> И наоборот, строки кода, которые не возвращают значения, называются операторами или утверждениями
> и используются для получения побочных эффектов.
>
> По мере погружения в Scala можно обнаружить, что пишется больше выражений и меньше утверждений.


---

**Ссылки:**

- [Scala3 book, taste Control Structures](https://docs.scala-lang.org/scala3/book/taste-control-structures.html)
- [Scala3 book, Control Structures](https://docs.scala-lang.org/scala3/book/control-structures.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/changed-features/match-syntax.html)
