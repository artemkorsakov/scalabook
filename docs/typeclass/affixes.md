# Значение аффиксов Scala

[Перевод статьи "Build your vocabulary with Scala affixes", автор Ross A. Baker][Affixes]

Ниже приводится объяснение некоторых префиксов и суффиксов в Scala.

## Префиксы

### bi-

**bi** - значит "два". 
Обычно префикс означает обобщение одного параметра типа до двух. 
Это полезно, когда есть канал ошибок, например `Either`.

[Functor](monad/functor.md) против [Bifunctor](bifunctor/bifunctor.md):

```scala
trait Functor[F[_]]:
  def map[A, B](fa: F[A])(f: A => B): F[B]

trait Bifunctor[F[_, _]]:
  def bimap[A, B, C, D](fab: F[A, B])(f: A => C, g: B => D): F[C, D]
```

[Traverse](monad/traverse.md) против [Bitraverse](bifunctor/bitraverse.md).

```scala
trait Traverse[F[_]]:
  def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]

trait Bitraverse[F[_, _]]:
  def bitraverse[G[_]: Applicative, A, B, C, D](  fab: F[A, B])(f: A => G[C], g: B => G[D]): G[F[C, D]]
```

Как ни странно, [Bimonad](monad/bimonad.md) не вводит второй параметр, 
а скорее объединяет категорию и ее двойственность.

```scala
trait Bimonad[F[_]] extends Monad[F], Comonad[F]
```

---

**Ссылки:**

- [Build your vocabulary with Scala affixes][Affixes]

[Affixes]: https://rossabaker.com/blog/scala-affixes
