# Semigroup

`(S, +)` является полугруппой (_semigroup_) для множества `S` и операции `+`, 
если удовлетворяет следующим свойствам для любых `x, y, z ∈ S`:
- Closure (замыкание): `x + y ∈ S`
- Associativity (ассоциативность): `(x + y) + z = x + (y + z)`

Также говорится, что _S образует полугруппу относительно +_.

## Описание

```scala
trait Semigroup[A]:
  def combine(x: A, y: A): A
```

## Примеры

### Натуральные числа N образуют полугруппу относительно сложения

```scala
given sumSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x + y
```

### Натуральные числа N образуют полугруппу относительно умножения

```scala
given productSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x * y
```

### Строки образуют полугруппу относительно конкатенации

```scala
given stringSemigroupInstance: Semigroup[String] = (x: String, y: String) => x + y
```

### [Последовательность](../../scala/collections) образует полугруппу относительно операции объединения

```scala
given listSemigroupInstance[T]: Semigroup[List[T]] =
  (x: List[T], y: List[T]) => x ++ y
```

### [Кортеж](../../scala/collections/tuple) от двух и более полугрупп также является полугруппой

```scala
given nestedSemigroupInstance[A, B](using aSemigroup: Semigroup[A], bSemigroup: Semigroup[B]): Semigroup[(A, B)] =
  (x: (A, B), y: (A, B)) => (aSemigroup.combine(x._1, y._1), bSemigroup.combine(x._2, y._2))
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonoid%2FSemigroup.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonoid%2FSemigroupSuite.scala)


## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*

List(1, 2) |+| List(3)      // List(1, 2, 3)
List(1, 2) mappend List(3)  // List(1, 2, 3)
1 |+| 2                     // 3
```

## Реализация в Cats

```scala
import cats.*
import cats.implicits.*

1 |+| 2                                     // 3
("hello", 123) |+| ("world", 321)           // ("helloworld",444)
```


---

## References

- [Algebird](https://twitter.github.io/algebird/typeclasses/semigroup.html)
- [Cats](https://typelevel.org/cats/typeclasses/semigroup.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Semigroup.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monoid.html#Monoid)
- [Math world](https://mathworld.wolfram.com/Semigroup.html)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#definition-of-a-semigroup)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Semigroup.html)
