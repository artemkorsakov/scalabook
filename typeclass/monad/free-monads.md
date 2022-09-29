# Free Monad 

Свободная монада обеспечивает:
- абстрактное синтаксическое дерево (AST - _abstract syntax tree_) для выражения монадических операций
- API для написания интерпретаторов, которые придают значение этому AST


### Примеры свободных монад

##### Описание свободной монады

```scala
sealed trait Free[F[_], A]
final case class Unit[F[_], A](a: A) extends Free[F, A]
final case class FlatMap[F[_], A, B](a: Free[F, A], fx: A => Free[F, B]) extends Free[F, B]

given freeFunctor[F[_]]: Functor[[X] =>> Free[F, X]] with
  extension [A](as: Free[F, A])
    override def map[B](f: A => B): Free[F, B] =
      FlatMap(as, a => Unit(f(a)))

given freeMonad[F[_]]: Monad[[X] =>> Free[F, X]] with
  override def unit[A](a: => A): Free[F, A] = Unit(a)

  extension [A](fa: Free[F, A]) override def flatMap[B](f: A => Free[F, B]): Free[F, B] = FlatMap(fa, f)
```

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FFree.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FFreeSuite.scala)


---

**References:**
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp) 
- [Free Monads Are Simple](https://underscore.io/blog/posts/2015/04/14/free-monads-are-simple.html)
