# Группа Monoid

### Схема

![monoid](https://gitflic.ru/project/artemkorsakov/scalabook/blob/raw?file=images%2Fmonoid.png&commit=49443b1e0cdcfd8ed3c1ed480bdd142245d579aa)

### Использование

Моноиды обычно используются для сворачивания последовательностей. 
Например, вот как можно посчитать сумму чисел:

```scala
trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A

given sumMonoidInstance: Monoid[Int] with
  val empty = 0
  def combine(x: Int, y: Int): Int = x + y

def sum[A: Monoid](xs: List[A]): A =
  val m = summon[Monoid[A]]
  xs.foldLeft(m.empty)(m.combine)

sum(List(2, 6, 3, 17)) 
// val res0: Int = 28
```

##### Исходный код группы

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/file?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonoid&plain=1)
[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/file?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonoid)

---

**References:**
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/sum+function.html)
