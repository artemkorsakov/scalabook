# Semigroup

`(S, +)` является полугруппой (_semigroup_) для множества `S` и операции `+`, 
если удовлетворяет следующим свойствам для любых `x, y, z ∈ S`:
- Closure (замыкание): `x + y ∈ S`
- Associativity (ассоциативность): `(x + y) + z = x + (y + z)`

Также говорится, что _S образует полугруппу относительно +_.


### Примеры полугрупп

##### Описание полугруппы

```scala
trait Semigroup[A]:
  def combine(x: A, y: A): A
```

##### Натуральные числа N образуют полугруппу относительно сложения

```scala
given sumSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x + y
```

##### Натуральные числа N образуют полугруппу относительно умножения

```scala
given productSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x * y
```

##### Строки образуют полугруппу относительно конкатенации

```scala
given stringSemigroupInstance: Semigroup[String] = (x: String, y: String) => x + y
```

##### [Последовательность](../../scala/collections) образует полугруппу относительно операции объединения

```scala
given listSemigroupInstance[T]: Semigroup[List[T]] =
  (x: List[T], y: List[T]) => x ++ y
```

##### [Кортеж](../../scala/collections/tuple) от двух и более полугрупп также является полугруппой

```scala
given nestedSemigroupInstance[A, B](using aSemigroup: Semigroup[A], bSemigroup: Semigroup[B]): Semigroup[(A, B)] =
  (x: (A, B), y: (A, B)) => (aSemigroup.combine(x._1, y._1), bSemigroup.combine(x._2, y._2))
```

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonoid%2FSemigroup.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonoid%2FSemigroupSuite.scala)


### Реализации полугрупп в различных библиотеках



---

**References:**
- [Algebird](https://twitter.github.io/algebird/typeclasses/semigroup.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
