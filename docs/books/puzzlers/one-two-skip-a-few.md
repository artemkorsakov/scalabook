# One, Two, Skip a Few

```scala
val oneTwo = Seq(1, 2).permutations
// oneTwo: Iterator[Seq[Int]] = empty iterator
if oneTwo.length > 0 then
  println("Permutations of 1 and 2:")
  oneTwo foreach println
// Permutations of 1 and 2:

val threeFour = Seq(3, 4).permutations
// threeFour: Iterator[Seq[Int]] = empty iterator
if threeFour.nonEmpty then
  println("Permutations of 3 and 4:")
  threeFour foreach println
// Permutations of 3 and 4:
// List(3, 4)
// List(4, 3)
```


Тип результата метода `permutations`, даже когда он вызывается для `Seq`, как здесь, является `Iterator`. 
Как объясняется в Scaladoc для `Iterator`: 
«никогда не следует использовать итератор после вызова для него метода. 
Два наиболее важных исключения также являются единственными абстрактными методами: `next` и `hasNext`». 
В первом примере мы игнорируем это правило и пытаемся пройтись по элементам итератора после вызова метода `length`. 
Вызов `length` фактически исчерпывает итератор, поэтому к тому времени, 
когда мы попытаемся напечатать элементы, их не останется. 
Во втором случае нам повезло, потому что `nonEmpty` реализован как `hasNext`. 
Таким образом, не вызываются никакие другие методы, кроме разрешенных. 
Затем можно успешно напечатать каждый из элементов, как и предполагалось.


---

**Ссылки:**

- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-048)
