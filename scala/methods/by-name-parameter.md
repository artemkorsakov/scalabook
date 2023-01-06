# Параметры по имени

Параметры по имени - это такие параметры, которые вычисляются только при использовании. 
Они указываются с помощью символа "стрелка" - `=>` 
Пример:

```scala
def or(a: Boolean, b: => Int): Int =
  if a then 1 else b

def b: Int =
  println("I'm calculated")
  2

or(true, b)
// res0: Int = 1
or(false, b)
// I'm calculated
// res1: Int = 2
```

В первом случае `b` не вычислялся, потому что он не используется при `a = true`.


---

**References:**
- [Scala3 book, Method Features](https://docs.scala-lang.org/scala3/book/methods-most.html)
