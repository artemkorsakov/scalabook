# Значение аффиксов Scala

[Перевод статьи "Build your vocabulary with Scala affixes", автор Ross A. Baker][Affixes]

Ниже приводится объяснение некоторых префиксов и суффиксов в Scala.

## Префиксы

### bi-

**bi** - значит "два". 
Обычно префикс означает обобщение одного параметра типа до двух. 
Это полезно, когда есть канал ошибок, например `Either`.

[Functor](monad/functor.md) против [Bifunctor](bifunctor/bifunctor.md):

```dotty
trait Functor[F[_]]:
  def map[A, B](fa: F[A])(f: A => B): F[B]

trait Bifunctor[F[_, _]]:
  def bimap[A, B, C, D](fab: F[A, B])(f: A => C, g: B => D): F[C, D]
```

[Traverse](monad/traverse.md) против [Bitraverse](bifunctor/bitraverse.md).

```dotty
trait Traverse[F[_]]:
  def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]

trait Bitraverse[F[_, _]]:
  def bitraverse[G[_]: Applicative, A, B, C, D](  fab: F[A, B])(f: A => G[C], g: B => G[D]): G[F[C, D]]
```

Как ни странно, [Bimonad](monad/bimonad.md) не вводит второй параметр, 
а скорее объединяет категорию и ее двойственность.

```dotty
trait Bimonad[F[_]] extends Monad[F], Comonad[F]
```

### co-

**co**-представляет собой [категориальную двойственность](index.md). 
Наивный способ думать об этом — «переворачивать стрелки».

`FlatMap` (другое имя - [Bind](monad/bind.md)) и `CoflatMap` (другое имя - [Co-Bind](monad/co-bind.md)). 

```dotty
trait FlatMap[F[_]]:
  // F[F[A]] → F[A]
  def flatten[A](ffa: F[F[A]]): F[A]

trait CoflatMap[F[_]]:
  // F[F[A]] ← F[A]
  def coflatten[A](fa: F[A]): F[F[A]]
```

Есть еще Kleisli и Co-Kleisli. 
Соответствующие примеры есть в [Cats](https://typelevel.org/cats/datatypes/kleisli.html) и в [ScalaZ](https://github.com/scalaz/scalaz/blob/v6.0.4/core/src/main/scala/scalaz/Cokleisli.scala#L20).

```dotty
// A → F[B]
final case class Kleisli[F[_], -A, B](run: A => F[B])
// A ← F[B]
final case class Cokleisli[F[_], B, A](run: F[B] => A)
// equivalently
final case class Cokleisli[F[_], A, B](run: F[A] => B)
```

### contra-

[Functor](monad/functor.md) и [Contra-Functor](monad/contravariant-functor.md):

```dotty
trait Functor[F[_]]:
  def map[A, B](fa: F[A])(f: A => B): F[B]

trait Contravariant[F[_]]:
  def contramap[A, B](fa: F[A])(f: B => A): F[B]
```

### di- 

Принимая во внимание, что `bimap` отображает обе стороны, `dimap` противопоставляет одну сторону и отображает другую. 
Функции с одним аргументом — это простой пример [Arrow](arrow/arrow.md) и [Profunctor](arrow/profunctor.md).

```dotty
trait Arrow[F[_, _]]:
  def dimap[A, B, C, D](fab: F[A, B])(f: C => A)(g: B => D): F[C, D]
```

### flat-

`flat` - сглаживает сущности.

```dotty
trait FlatMap[F[_]]:
  def     map[A, B](fa: F[A])(f: (A) =>   B ): F[B]
  def flatMap[A, B](fa: F[A])(f: (A) => F[B]): F[B]
  //      tap[A, B](fa: F[A])(f: (A) =>   B ): F[A]
  def flatTap[A, B](fa: F[A])(f: (A) => F[B]): F[A]
```

`tap` - метод, используемый, когда нужно отбросить результат и не фиксировать эффект 
(это уже за пределами мира функционального программирования). 
Но стандартная библиотека [определяет tap](https://scala-lang.org/api/3.3.0/scala/util/ChainingOps.html).

### par- 

`par` - для параллельных операций. 
В стандартной библиотеке параллельные коллекции [выделены в отдельный модуль](https://github.com/scala/scala-parallel-collections).

```dotty
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

```dotty
case class OptionT[F[_], A](value: F[Option[A]]):
  def     flatMap[B](f: (A) => OptionT[F, B])(implicit F: Monad[F]): OptionT[F, B]
  def semiflatMap[B](f: (A) =>         F [B])(implicit F: Monad[F]): OptionT[F, B]
```

### sub- 

`sub-` используется различными преобразователями монад для операций, 
которые возвращают внутреннюю монаду вместо преобразованной монады:

```dotty
case class OptionT[F[_], A](value: F[Option[A]]):
  def    flatMap[B](f: (A) => OptionT[F, B])(implicit F: Monad[F]):   OptionT[F, B]
  def subflatMap[B](f: (A) => Option [   B])(implicit F: Functor[F]): OptionT[F, B]
```

## Суффиксы 

### -A

`-A` для операций [Applicative](monad/applicative.md).

```dotty
trait Applicative[F[_]]:
  def replicateA[A](n: Int, fa: F[A]): F[List[A]]
  def unlessA[A](cond: Boolean)(f: => F[A]): F[Unit]
  def whenA[A](cond: Boolean)(f: => F[A]): F[Unit]
```

### -F

`-F` для операций [Functor-а](monad/functor.md).

```dotty
trait Functor[F[_]]:
  def ifF[A](fb: F[Boolean])(ifTrue: => A, ifFalse: => A): F[A]
```

`-F` также используется для преобразования значений в более сложный тип. 
В этом случае можно думать об **F-"эффекте"**.
Иногда `Functor` - это все, что нужно. Иногда необходимо нечто большее.

```dotty
object Kleisli:
  def liftF[F[_], A, B](x: F[B]): Kleisli[F, A, B]

object OptionT:
  def liftF[F[_], A](fa: F[A])(F: Functor[F]): OptionT[F, A]

object IorT:
  def liftF[F[_], A, B](fb: F[B])(F: Applicative[F]): IorT[F, A, B]

object ContT:
  def liftF[F[_], A, B](mb: M[B])(M: FlatMap[M]): ContT[M, A, B]
```

Вещи, называемые `eval` в Cats-Effect и FS2, очень похожи на `liftF`, но нарушает нашу этимологию.

```dotty
object Resource:
  def eval[F[_], A](fa: F[A]): Resource[F, A]

object Stream:
  def eval[F[_], O](fo: F[O]): Stream[F, O]
```

### -K 

`-K` предназначен для "высшего рода" и, как правило, не зависит от того, "что внутри коробки".

[MonoidK](https://typelevel.org/cats/typeclasses/monoidk.html) и [Monoid](monoid/monoid.md):

```dotty
trait Monoid[A]:
  def combine(x: A, y: A): A

trait MonoidK[F[_]]:
  def combineK[A](x: F[A], y: F[A]): F[A]
```

**FunctionK** и **Function**:

```dotty
trait Function1[-T1, +R]: // или =>
  def apply(v1: T1): R

trait FunctionK[F[_], G[_]]: // или ~>
  def apply[A](fa: F[A]): G[A]
```

### -L, -R

`-L` и `-R` обозначают `Left` и `Right`.

```dotty
trait Applicative[F[_]]:
  // Более известно как `<*`
  def productL[A, B](fa: F[A])(fb: F[B]): F[A]
  // Более известно как `*>`
  def productR[A, B](fa: F[A])(fb: F[B]): F[B]
```

### -M

`-M` для монадических операций.

```dotty
trait Monad[F[_]]:
  def ifM(fa: F[Boolean])(ifTrue: => F[B], ifFalse: => F[B]): F[B]
  def untilM[G[_], A](f: F[A])(cond: => F[Boolean])(implicit G: Alternative[G]): F[G[A]]
  def whileM[G[_], A](p: F[Boolean])(body: => F[A])(implicit G: Alternative[G]): F[G[A]]
```

### -T

`-T` предназначен для преобразователей монад.

```dotty
final case class EitherT[F[_], A, B](value: F[Either[A, B]])
final case class IdT[F[_], A](value: F[A])
final case class IorT[F[_], A, B](value: F[Ior[A, B]])
final case class OptionT[F[_], A](value: F[Option[A]])
final case class WriterT[F[_], L, V](run: F[(L, V)])
// И еще несколько псевдонимов
type ReaderT[F[_], -A, B] = Kleisli[F, A, B]
type StateT[F[_], S, A] = IndexedStateT[F, S, S, A]
type StoreT[W[_], S, A] = RepresentableStoreT[W, [β$2$](S) => β$2$, S, A]
```

### _-

Суффикс подчеркивания обычно означает: "Меня волнует эффект, а не результат внутри".
Реализации могут быть оптимизированы, если известно, что вызывающий объект все равно проигнорирует это.

```dotty
trait Traverse[F[_]]:
  def traverse [G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]
  def traverse_[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[Unit]
```

---

**Ссылки:**

- [Build your vocabulary with Scala affixes][Affixes]

[Affixes]: https://rossabaker.com/blog/scala-affixes
