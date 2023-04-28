# Параметры по имени

Параметры по имени - это такие параметры, которые вычисляются только при использовании. 
Они указываются с помощью символа "стрелка" - `=>` 
Пример:

```scala mdoc
def or(a: Boolean, b: => Int): Int =
  if a then 1 else b

def b: Int =
  println("I'm calculated")
  2

or(true, b)
or(false, b)
```

В первом случае `b` не вычислялся, потому что он не используется при `a = true`.


---

**Ссылки:**

- [Scala3 book, Method Features](https://docs.scala-lang.org/scala3/book/methods-most.html)
