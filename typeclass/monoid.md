# Группа Monoid

### Схема

![monoid](https://gitflic.ru/project/artemkorsakov/scalabook/blob/raw?file=images%2Fmonoid.png&commit=3dc3f31a18306cbadb9e88a2ba336c6cff6cb92f)

### Использование

Моноиды обычно используются для сворачивания последовательностей. 
Например, вот как можно посчитать произведение чисел:

```scala
trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A

given Monoid[Int] with
  val empty = 1
  def combine(x: Int, y: Int): Int = x * y

def fold[A: Monoid](xs: List[A]): A =
  val m = summon[Monoid[A]]
  xs.foldLeft(m.empty)(m.combine)

fold(List(1, 2, 3, 4, 5))
// val res0: Int = 120
```

Обычно библиотеки определяют "свой" оператор для моноидальной операции `combine`, например:

```scala
trait Semigroup[A]:
  def combine(x: A, y: A): A
  
  extension (x: A)
    def |+|(y: A) = combine(x, y)

...

3 |+| 4
// val res1: Int = 12
```



### Исходный код группы

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/file?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonoid&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/file?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonoid)

---

**References:**
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/sum+function.html)
