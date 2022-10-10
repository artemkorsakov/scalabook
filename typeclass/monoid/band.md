# Band

_Band_ — это полугруппа, которая также является идемпотентной, 
т.е. добавление значения к самому себе приводит к тому же значению.

`Band` должна удовлетворять следующим законам (+ закон `Semigroup`):
- Associativity (ассоциативность): `(x + y) + z = x + (y + z)`
- Idempotency (идемпотентность): для любых `a ∈ M` такое, что `a + a = a`

### Примеры

##### Описание

```scala
trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Band[A] extends Semigroup[A]
```

##### Множества

Некоторые операции над множествами являются идемпотентными: объединение, пересечение и т.п.
Например, объединение множеств образуют `Band`:

```scala
given setBandInstance[A]: Band[Set[A]] =
  (x: Set[A], y: Set[A]) => x ++ y
```


[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonoid%2FBand.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonoid%2FBandSuite.scala)


---

**References:**
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Band.html)
