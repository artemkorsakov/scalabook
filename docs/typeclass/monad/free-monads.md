# Свободная монада

Свободная монада обеспечивает:

- абстрактное синтаксическое дерево (AST - _abstract syntax tree_) для выражения монадических операций
- API для написания интерпретаторов, которые придают значение этому AST


## Описание свободной монады

```dotty
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


---

**Ссылки:**

- [Free Monads Are Simple](https://underscore.io/blog/posts/2015/04/14/free-monads-are-simple.html)
- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Herding Cats](http://eed3si9n.com/herding-cats/Free-monads.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Free+Monad.html)
- [Stackless Scala](../../fp/trampolining.md)
- [Why free monads matter](https://www.haskellforall.com/2012/06/you-could-have-invented-free-monads.html)
