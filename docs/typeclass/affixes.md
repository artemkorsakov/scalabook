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

### co-

**co**-представляет собой [категориальную двойственность](../typeclass.md). 
Наивный способ думать об этом — «переворачивать стрелки».

`FlatMap` (другое имя - [Bind](monad/bind.md)) и `CoflatMap` (другое имя - [Co-Bind](monad/co-bind.md)). 

```scala
trait FlatMap[F[_]]:
  // F[F[A]] → F[A]
  def flatten[A](ffa: F[F[A]]): F[A]

trait CoflatMap[F[_]]:
  // F[F[A]] ← F[A]
  def coflatten[A](fa: F[A]): F[F[A]]
```

Есть еще Kleisli и Co-Kleisli. 
Соответствующие примеры есть в [Cats](https://typelevel.org/cats/datatypes/kleisli.html) и в [ScalaZ](https://github.com/scalaz/scalaz/blob/v6.0.4/core/src/main/scala/scalaz/Cokleisli.scala#L20).

```scala
// A → F[B]
final case class Kleisli[F[_], -A, B](run: A => F[B])
// A ← F[B]
final case class Cokleisli[F[_], B, A](run: F[B] => A)
// equivalently
final case class Cokleisli[F[_], A, B](run: F[A] => B)
```

### contra-

[Functor](monad/functor.md) и [Contra-Functor](monad/contravariant-functor.md):

```scala
trait Functor[F[_]]:
  def map[A, B](fa: F[A])(f: A => B): F[B]

trait Contravariant[F[_]]:
  def contramap[A, B](fa: F[A])(f: B => A): F[B]
```

### di- 

Принимая во внимание, что `bimap` отображает обе стороны, `dimap` противопоставляет одну сторону и отображает другую. 
Функции с одним аргументом — это простой пример [Arrow](arrow/arrow.md) и [Profunctor](arrow/profunctor.md).

```scala
trait Arrow[F[_, _]]:
  def dimap[A, B, C, D](fab: F[A, B])(f: C => A)(g: B => D): F[C, D]
```

### flat-

`flat` - сглаживает сущности.

```scala
trait FlatMap[F[_]]:
  def     map[A, B](fa: F[A])(f: (A) =>   B ): F[B]
  def flatMap[A, B](fa: F[A])(f: (A) => F[B]): F[B]
  //      tap[A, B](fa: F[A])(f: (A) =>   B ): F[A]
  def flatTap[A, B](fa: F[A])(f: (A) => F[B]): F[A]
```

`tap` - метод, используемый, когда нужно отбросить результат и не фиксировать эффект 
(это уже за пределами мира [функционального программирования](../fp.md)). 
Но стандартная библиотека [определяет tap](https://scala-lang.org/api/3.3.0/scala/util/ChainingOps.html).

### par- 

`par` - для параллельных операций. 
В стандартной библиотеке параллельные коллекции [выделены в отдельный модуль](https://github.com/scala/scala-parallel-collections).

```scala
trait ParIterable[+T]
trait ParSeq[+T]
trait ParMap[K, +V]
trait ParSet[T]
```

В Cats можно использовать класс типов [Parallel](https://typelevel.org/cats/typeclasses/parallel.html) 
для "распараллеливания" некоторых операций. 
Для некоторых монад, таких как IO, это относится к параллельному вычислению. 
В `Parallel[Either]` речь идет о накоплении ошибок, а в `Parallel[List]` - о сжатии. 
В этих случаях ничего не говорится о многопоточности, 
но они делегируются другому экземпляру `Applicative`, 
чтобы избежать [требования последовательного закона монад](https://typelevel.org/cats/datatypes/validated.html#of-flatmaps-and-eithers).

### semi- 

`semi-` используется различными преобразователями монад для операций, 
которые возвращают базовую монаду вместо преобразованной:

```scala
case class OptionT[F[_], A](value: F[Option[A]]):
  def     flatMap[B](f: (A) => OptionT[F, B])(implicit F: Monad[F]): OptionT[F, B]
  def semiflatMap[B](f: (A) =>         F [B])(implicit F: Monad[F]): OptionT[F, B]
```

### sub- 

`sub-` используется различными преобразователями монад для операций, 
которые возвращают внутреннюю монаду вместо преобразованной монады:

```scala
case class OptionT[F[_], A](value: F[Option[A]]):
  def    flatMap[B](f: (A) => OptionT[F, B])(implicit F: Monad[F]):   OptionT[F, B]
  def subflatMap[B](f: (A) => Option [   B])(implicit F: Functor[F]): OptionT[F, B]
```

## Суффиксы 

---

**Ссылки:**

- [Build your vocabulary with Scala affixes][Affixes]

[Affixes]: https://rossabaker.com/blog/scala-affixes
