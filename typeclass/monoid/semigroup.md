# Semigroup

`(S, +)` является полугруппой (_semigroup_) для множества `S` и операции `+`, 
если удовлетворяет следующим свойствам для любых `x, y, z ∈ S`:
- Closure (замыкание): `x + y ∈ S`
- Associativity (ассоциативность): `(x + y) + z = x + (y + z)`

Также говорится, что _S образует полугруппу относительно +_.


### Примеры полугрупп

```scala
trait Semigroup[A]:
  def combine(x: A, y: A): A
```

- Числа образуют полугруппу относительно сложения
  ```scala
  given sumSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x + y
  ```

- Числа образуют полугруппу относительно умножения
  ```scala
  given productSemigroupInstance: Semigroup[BigInt] = (x: BigInt, y: BigInt) => x * y
  ```
  
- Строки образуют полугруппу при конкатенации
  ```scala
  given stringSemigroupInstance: Semigroup[String] = (x: String, y: String) => x + y
  ```

- Последовательность образует полугруппу относительно операции объединения
  ```scala
  given listSemigroupInstance[T]: Semigroup[List[T]] =
    (x: List[T], y: List[T]) => x ++ y
  ```

- [Кортеж](../../scala/collections/tuple.md) от двух и более полугрупп также является полугруппой
  ```scala
  given nestedSemigroupInstance[A, B](using aSemigroup: Semigroup[A], bSemigroup: Semigroup[B]): Semigroup[(A, B)] =
    (x: (A, B), y: (A, B)) => (aSemigroup.combine(x._1, y._1), bSemigroup.combine(x._2, y._2))
  ```

### Реализации полугрупп в различных библиотеках



---

**References:**
- [Algebird](https://twitter.github.io/algebird/typeclasses/semigroup.html)
