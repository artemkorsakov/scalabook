# Beep Beep...Reversing

```scala
import collection.SortedSet
val ints = SortedSet(-1, 1, 2)(summon[Ordering[Int]].reverse)
println(ints.filter(_ > 0).head)
// 2
println(ints.collect { case n if n > 0 => n }.head)
// 1
```

И `Set.filter`, и `Set.collect` сохраняют тип исходной коллекции и возвращают новые отсортированные наборы. 
Однако, как и другие методы, которые могут возвращать элементы другого типа, 
такие как `map` и `flatMap`, метод `collect` не сохраняет порядок исходной коллекции. 
Есть перегруженные методы, принимающие `Ordering`, например, `Set.collect`:

```scala
println(ints.collect { case n if n > 0 => n }(summon[Ordering[Int]].reverse).head)
// 2
```

или

```scala
import collection.SortedSet
given Ordering[Int] = math.Ordering.Int.reverse
val ints = SortedSet(-1, 1, 2)
ints.collect { case n if n > 0 => n }.head
// res4: Int = 2
```


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-069)
