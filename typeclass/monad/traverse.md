# Traverse

Предположим, что есть два функтора `F` и `G`.
`Traversable` позволяет менять местами "обертку" функторов между собой, 
т.е. реализует операции `traverse` = `(F[A], A => G[B]) -> G[F[B]]` и `sequence` = `F[G[A]] -> G[F[A]]`

С помощью `traverse` можно выразить и `map`, и `foldRight`, а также `sequence`, отображающий `F[G[A]]` в `G[F[A]]`.

`Traversable` должен удовлетворять следующим законам:

- Обход Id эквивалентен `Functor#map`:
  ```scala
  traverse[F, Id, A, B](fa, a => Id(f(a))).value == fa.map(f)
  ```
- Два последовательно зависимых эффекта могут быть объединены в один, их композицию:
  ```scala
  val optFb: G[F[B]] = traverse[F, G, A, B](fa, a => unit(f(a)))
  val optListFc1: G[H[F[C]]] =
    map[G, F[B], H[F[C]]](optFb, fb => traverse[F, H, B, C](fb, b => unit(g(b))))
  val optListFc2: G[H[F[C]]] =
    traverse[F, [X] =>> G[H[X]], A, C](fa, a => map[G, B, H[C]](unit(f(a)), b => unit(g(b))))
  optListFc1 == optListFc2
  ```
- Обход с помощью функции `unit` аналогичен прямому применению функции `unit`:
  ```scala
  traverse[F, G, A, A](fa, a => unit(a)) == unit[G, F[A]](fa)
  ```
- Два независимых эффекта могут быть объединены в один эффект, их произведение
  ```scala
  type GH[A] = (G[A], H[A])
  val t1: GH[F[B]] = (traverse[F, G, A, B](fa, a => unit(f(a))), traverse[F, H, A, B](fa, a => unit(f(a))))
  val t2: GH[F[B]] = traverse[F, GH, A, B](fa, a => (unit(f(a)), unit(f(a))))
  t1 == t2
  ```


### Примеры

##### Описание 

```scala
trait Traverse[F[_]] extends Functor[F], Foldable[F]:
  self =>

  extension [A](fa: F[A])
    def traverse[G[_]: Applicative, B](f: A => G[B]): G[F[B]]

    override def map[B](f: A => B): F[B] = traverse(a => Id(f(a))).value

    override def foldRight[B](init: B)(f: (A, B) => B): B =
      traverse(a => State[B, A]((b: B) => (f(a, b), a))).run(init)._1

  def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]] =
    fga.traverse(ga => ga)
```

##### "Обертка"

```scala
case class Id[A](value: A)

given Traverse[Id] with
  extension [A](fa: Id[A])
    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Id[B]] =
      f(fa.value).map(b => Id(b))
```

##### [Кортеж](../../scala/collections/tuple) от двух и более элементов

```scala
given Traverse[[X] =>> (X, X)] with
  extension [A](fa: (A, A))
    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[(B, B)] =
      val g = summon[Applicative[G]]
      val func: G[B => B => (B, B)] = g.unit(b1 => b2 => (b1, b2))
      g.apply(g.apply(func)(f(fa._1)))(f(fa._2))

given Traverse[[X] =>> (X, X, X)] with
  extension [A](fa: (A, A, A))
    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[(B, B, B)] =
      val g = summon[Applicative[G]]
      val func: G[B => B => B => (B, B, B)] = g.unit(b1 => b2 => b3 => (b1, b2, b3))
      g.apply(g.apply(g.apply(func)(f(fa._1)))(f(fa._2)))(f(fa._3))
```

##### [Option](../../scala/fp/functional-error-handling)

```scala
given Traverse[Option] with
  extension [A](fa: Option[A])
    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Option[B]] =
      fa match
        case Some(a) => f(a).map(Some(_))
        case None    => summon[Applicative[G]].unit(None)
```

##### [Последовательность](../../scala/collections)

```scala
given Traverse[List] with
  extension [A](fa: List[A])
    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[List[B]] =
      val g = summon[Applicative[G]]
      fa.foldRight(g.unit(List[B]()))((a, acc) => f(a).map2(acc)(_ :: _))
```

##### Дерево

В области видимости должны быть доступны `idApplicative` and `listTraverse`

```scala
case class Tree[+A](head: A, tail: List[Tree[A]])

given Traverse[Tree] with
  extension [A](ta: Tree[A])
    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Tree[B]] =
      f(ta.head).map2(ta.tail.traverse(a => a.traverse(f)))(Tree(_, _))

val tree = Tree(0, List(Tree(1, List(Tree(2, Nil)))))
tree.traverse(a => Id(a + 1))
// val res0: Id[Tree[Int]] = Id(Tree(1,List(Tree(2,List(Tree(3,List()))))))
```

##### [Map](../../scala/collections/maps)

```scala
given mapTraverse[K]: Traverse[[X] =>> Map[K, X]] with
  extension [A](m: Map[K, A])
    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Map[K, B]] =
      m.foldLeft(summon[Applicative[G]].unit(Map.empty[K, B])) { case (acc, (k, a)) =>
        acc.map2(f(a))((m, b) => m + (k -> b))
      }
```

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FTraverse.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FTraverseSuite.scala)


### Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._

List(1, 2, 3) traverse { x => (x > 0) option (x + 1) }  // Some(List(2, 3, 4))
List(1.some, 2.some).sequence                           // Some(List(1, 2))
1.success[String].leaf.sequenceU map {_.drawTree}       // Success(1)
```


---

**References:**
- [Tour of Scala](https://tourofscala.com/scala/traversable)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Traverse.html)
