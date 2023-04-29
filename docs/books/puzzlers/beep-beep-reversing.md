# Beep Beep...Reversing

```scala mdoc:silent
import collection.SortedSet
val ints = SortedSet(-1, 1, 2)(summon[Ordering[Int]].reverse)
```
```scala mdoc
println(ints.filter(_ > 0).head)
println(ints.collect { case n if n > 0 => n }.head)
```

И `Set.filter`, и `Set.collect` сохраняют тип исходной коллекции и возвращают новые отсортированные наборы. 
Однако, как и другие методы, которые могут возвращать элементы другого типа, 
такие как `map` и `flatMap`, метод `collect` не сохраняет порядок исходной коллекции. 
Есть перегруженные методы, принимающие `Ordering`, например, `Set.collect`:

```scala mdoc
println(ints.collect { case n if n > 0 => n }(summon[Ordering[Int]].reverse).head)
```

или

```scala mdoc:reset
import collection.SortedSet
given Ordering[Int] = math.Ordering.Int.reverse
val ints = SortedSet(-1, 1, 2)
ints.collect { case n if n > 0 => n }.head
```


---

**Ссылки:**

- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-069)
