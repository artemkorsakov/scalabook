# ApplicativePlus

Инвариантный функтор поддерживает операцию `xmap`, которая преобразует `F[A]` в `F[B]` с учетом двух функций: `A => B` и `B => A`. 

Инвариантный функтор должен удовлетворять двум законам: 
- Identity (тождественность): Если определен метод идентификации `identity` такой, что: `identity(a) == a`,
  тогда `xmap(ma)(identity, identity) == ma`.
- Composition (композиция) - `xmap(xmap(ma, f1, g1), f2, g2) == xmap(ma, f2 compose f1, g1, compose g2)`

Также известен как экспоненциальный функтор.

### Примеры инвариантных функторов

##### Описание инвариантного функтора

```scala
trait InvariantFunctor[F[_]]:
  extension [A](fa: F[A]) 
    def xmap[B](f: A => B, g: B => A): F[B]
```

##### "Обертка"

```scala
case class Id[A](value: A)

given idInvariantFunctor: InvariantFunctor[Id] with
  extension [A](fa: Id[A]) 
    override def xmap[B](f: A => B, g: B => A): Id[B] = Id(f(fa.value))
```

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FInvariantFunctor.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FInvariantFunctorSuite.scala)


### Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._

// ... Все операции родителей

for { n <- List(1, 2); ch <- List('a', 'b') } yield (n, ch)     // List((1,a), (1,b), (2,a), (2,b))
(for { a <- (_: Int) * 2; b <- (_: Int) + 10 } yield a + b)(3)  // 19
List(1, 2) filterM { x => List(true, false) }                   // List(List(1, 2), List(1), List(2), List())
```


---

**References:**
- [Scalaz API](https://javadoc.io/static/org.scalaz/scalaz-core_3/7.3.6/scalaz/InvariantFunctor.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monad.html)
