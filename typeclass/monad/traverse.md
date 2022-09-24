# Traverse

Предположим, что есть два функтора `F` и `G`.
`Traversable` позволяет менять местами "обертку" функторов между собой, 
т.е. реализует операцию `traverse` = `F[G[A]] -> G[F[A]]`


### Примеры traversable

##### Описание Traverse

```scala
trait Functor[F[_]]:
  extension [A](fa: F[A]) def map[B](f: A => B): F[B]

trait Foldable[F[_]]:
  extension [A](fa: F[A]) 
    def foldRight[B](init: B)(f: (A, B) => B): B

trait Applicative[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  extension [A](fa: F[A])
    def map[B](f: A => B): F[B] =
      apply(unit(f))(fa)
      
    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      apply(apply(unit(f.curried))(fa))(fb)  

trait Traverse[F[_]] extends Functor[F], Foldable[F]:
  self =>

  extension [A](fa: F[A])
    def traverse[G[_]: Applicative, B](f: A => G[B]): G[F[B]]
```

##### "Обертка"

```scala
case class Id[A](value: A)

given idTraverse: Traverse[Id] with
  extension [A](fa: Id[A])
    override def map[B](f: A => B): Id[B] = Id(f(fa.value))

    override def foldRight[B](init: B)(f: (A, B) => B): B =
      f(fa.value, init)

    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Id[B]] =
      f(fa.value).map(b => Id(b))
```

##### [Кортеж](../../scala/collections/tuple) от двух и более элементов

```scala
given tuple2Traverse: Traverse[[X] =>> (X, X)] with
  extension [A](fa: (A, A))
    override def map[B](f: A => B): (B, B) = (f(fa._1), f(fa._2))

    override def foldRight[B](init: B)(f: (A, B) => B): B =
      f(fa._1, f(fa._2, init))

    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[(B, B)] =
      val g = summon[Applicative[G]]
      val func: G[B => B => (B, B)] = g.unit(b1 => b2 => (b1, b2))
      g.apply(g.apply(func)(f(fa._1)))(f(fa._2))
      
given tuple3Traverse: Traverse[[X] =>> (X, X, X)] with
  extension [A](fa: (A, A, A))
    override def map[B](f: A => B): (B, B, B) = (f(fa._1), f(fa._2), f(fa._3))

    override def foldRight[B](init: B)(f: (A, B) => B): B =
      val (a0, a1, a2) = fa
      val b0 = f(a2, init)
      val b1 = f(a1, b0)
      f(a0, b1)

    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[(B, B, B)] =
      val g = summon[Applicative[G]]
      val func: G[B => B => B => (B, B, B)] = g.unit(b1 => b2 => b3 => (b1, b2, b3))
      g.apply(g.apply(g.apply(func)(f(fa._1)))(f(fa._2)))(f(fa._3))
```

##### [Option](../../scala/fp/functional-error-handling)

```scala
given optionTraverse: Traverse[Option] with
  extension [A](fa: Option[A])
    override def map[B](f: A => B): Option[B] =
      fa match
        case Some(a) => Some(f(a))
        case None    => None

    override def foldRight[B](init: B)(f: (A, B) => B): B =
      fa match
        case Some(a) => f(a, init)
        case None    => init

    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Option[B]] =
      fa match
        case Some(a) => f(a).map(Some(_))
        case None    => summon[Applicative[G]].unit(None)
```

##### [Последовательность](../../scala/collections)

```scala
given listTraverse: Traverse[List] with
  extension [A](fa: List[A])
    override def map[B](f: A => B): List[B] = fa.map(f)

    override def foldRight[B](init: B)(f: (A, B) => B): B =
      fa.foldRight(init)(f)
      
    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[List[B]] =
      val g = summon[Applicative[G]]
      fa.foldRight(g.unit(List[B]()))((a, acc) => f(a).map2(acc)(_ :: _))
```

##### Дерево

В области видимости должны быть доступны `idApplicative` and `listTraverse`

```scala
case class Tree[+A](head: A, tail: List[Tree[A]])

given treeTraverse: Traverse[Tree] with
  extension [A](ta: Tree[A])
    override def map[B](f: A => B): Tree[B] =
      Tree(f(ta.head), ta.tail.map(tailTree => tailTree.map(f)))

    override def foldRight[B](init: B)(f: (A, B) => B): B =
      ta.tail.foldRight(f(ta.head, init))((treeA, b) => treeA.foldRight(b)(f))

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
    override def map[B](f: A => B): Map[K, B] =
      m.view.mapValues(f).toMap

    override def foldRight[B](init: B)(f: (A, B) => B): B =
      m.foldRight(init) { case ((_, a), b) => f(a, b) }

    override def traverse[G[_]: Applicative, B](f: A => G[B]): G[Map[K, B]] =
      m.foldLeft(summon[Applicative[G]].unit(Map.empty[K, B])) { case (acc, (k, a)) =>
        acc.map2(f(a))((m, b) => m + (k -> b))
      }
```

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FTraverse.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FTraverseSuite.scala)


### Реализации traversable в различных библиотеках


---

**References:**
- [Tour of Scala](https://tourofscala.com/scala/traversable)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
