# Monad Transformer

**Monad Transformer** (или **трансформер монад**) — это конструкция в функциональном программировании, 
позволяющая комбинировать несколько монад в одну, сохраняя их индивидуальные свойства. 
Это особенно полезно, когда требуется работать с вложенными контекстами, 
например, `Option[Future[A]]` или `Either[Error, Option[A]]`.

## Основные Monad Transformers в Scala

- **`OptionT[F[_], A]`**:
    - Комбинирует монаду `F` с `Option`.
    - Полезен для работы с типами вида `F[Option[A]]`.
- **`EitherT[F[_], E, A]`**:
    - Комбинирует монаду `F` с `Either[E, A]`.
    - Полезен для работы с типами вида `F[Either[E, A]]`.
- **`ReaderT[F[_], R, A]`** (также известный как `Kleisli`):
    - Комбинирует монаду `F` с `Reader` (функцией `R => A`).
    - Полезен для работы с типами вида `R => F[A]`.
- **`WriterT[F[_], L, A]`**:
    - Комбинирует монаду `F` с `Writer` (значением, сопровождаемым логом).
    - Полезен для работы с типами вида `F[(L, A)]`.
- **`StateT[F[_], S, A]`**:
    - Комбинирует монаду `F` с `State` (вычислениями, зависящими от состояния).
    - Полезен для работы с типами вида `S => F[(S, A)]`.

### Краткие примеры

- **Асинхронные вычисления с опциональным результатом**:
    - У вас есть асинхронная операция, которая возвращает `Future[Option[A]]`.
    - С помощью `OptionT` вы можете работать с этим типом как с монадой, не разворачивая вложенные контексты вручную.
- **Обработка ошибок в асинхронных вычислениях**:
    - У вас есть асинхронная операция, которая возвращает `Future[Either[Error, A]]`.
    - С помощью `EitherT` вы можете обрабатывать ошибки и асинхронные вычисления в одном контексте.
- **Работа с состоянием и логами**:
    - У вас есть вычисление, которое зависит от состояния (`State`) и сопровождается логами (`Writer`).
    - С помощью `StateT` и `WriterT` вы можете комбинировать эти контексты для удобной работы.

## Формальное определение

Monad Transformer — это тип `T[F[_], A]`, где:

- `F[_]` — это базовая монада (например, `Option`, `Future`, `Either`).
- `T` — это трансформер, который добавляет дополнительный слой контекста 
  (например, `OptionT` для `Option`, `EitherT` для `Either`).

Monad Transformer предоставляет методы для работы с вложенными монадами, 
такие как `map`, `flatMap`, и `pure`, но в контексте комбинированной монады.

## Код

```dotty
trait MonadTransformer[T[_[_], _], M[_]](using mMonad: Monad[M], tMonad: Monad[[X] =>> T[M, X]]):
  def lift[A](ma: M[A]): T[M, A]
```

### "Обертка"

```dotty
final case class IdT[M[_], A](run: M[Id[A]])

given idtMonad[M[_]](using outerMonad: Monad[M]): Monad[[X] =>> IdT[M, X]] with
  override def unit[A](a: => A): IdT[M, A] =
    IdT[M, A](outerMonad.unit(Id(a)))
  extension [A](fa: IdT[M, A])
    override def flatMap[B](f: A => IdT[M, B]): IdT[M, B] =
      IdT[M, B](fa.run.flatMap(ida => f(ida.value).run))

given idtMonadTransformer[M[_]](using outerMonad: Monad[M]): MonadTransformer[IdT, M] with
  override def lift[A](ma: M[A]): IdT[M, A] =
    IdT[M, A](ma.map(Id(_)))
```

### Option

```dotty
final case class OptionT[M[_], A](run: M[Option[A]])

given optionTMonad[M[_]](using outerMonad: Monad[M]): Monad[[X] =>> OptionT[M, X]] with
  override def unit[A](a: => A): OptionT[M, A] =
    OptionT[M, A](outerMonad.unit(Some(a)))

  extension [A](fa: OptionT[M, A])
    override def flatMap[B](f: A => OptionT[M, B]): OptionT[M, B] =
      OptionT(
        fa.run.flatMap {
          case Some(value) => f(value).run
          case None        => outerMonad.unit(None)
        }
      )
      
given optionTMonadTransformer[M[_]](using outerMonad: Monad[M]): MonadTransformer[OptionT, M] with
  override def lift[A](ma: M[A]): OptionT[M, A] =
    OptionT[M, A](ma.map(Some(_)))      
```

### Writer - функциональный журнал

```dotty
final case class WriterT[M[_], W, A](run: () => M[(W, A)])

given writerTMonad[M[_], W](using outerMonad: Monad[M], outerMonoid: Monoid[W]): Monad[[X] =>> WriterT[M, W, X]] with
  override def unit[A](a: => A): WriterT[M, W, A] =
    WriterT[M, W, A](() => outerMonad.unit((outerMonoid.empty, a)))

  extension [A](fa: WriterT[M, W, A])
    override def flatMap[B](f: A => WriterT[M, W, B]): WriterT[M, W, B] =
      WriterT[M, W, B] { () =>
        fa.run().flatMap { case (wa, a) =>
          f(a).run().map { case (wb, b) => (outerMonoid.combine(wa, wb), b) }
        }
      }
      
given writerTMonadTransformer[M[_], W](using
    outerMonad: Monad[M],
    outerMonoid: Monoid[W]
): MonadTransformer[[Y[_], X] =>> WriterT[Y, W, X], M] with
  override def lift[A](ma: M[A]): WriterT[M, W, A] =
    WriterT[M, W, A](() => ma.map(a => (outerMonoid.empty, a)))      
```

### State - функциональное состояние

```dotty
final case class StateT[M[_], S, A](run: S => M[(S, A)])

given stateTMonad[M[_], S](using outerMonad: Monad[M]): Monad[[X] =>> StateT[M, S, X]] with
  override def unit[A](a: => A): StateT[M, S, A] =
    StateT[M, S, A](s => outerMonad.unit((s, a)))

  extension [A](fa: StateT[M, S, A])
    override def flatMap[B](f: A => StateT[M, S, B]): StateT[M, S, B] =
      StateT[M, S, B] { s =>
        fa.run(s).flatMap { case (s1, a) => f(a).run(s1) }
      }
      
given stateTMonadTransformer[M[_], S](using outerMonad: Monad[M]): MonadTransformer[[Y[_], X] =>> StateT[Y, S, X], M] with
  override def lift[A](ma: M[A]): StateT[M, S, A] =
    StateT[M, S, A](s => ma.map(a => (s, a)))      
```

---

**Ссылки:**

- [A comprehensive introduction to Cats-mtl](https://typelevel.org/blog/2018/10/06/intro-to-mtl.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp) 
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monad+transformers.html)
- [Scalaz Monad Transformers](https://noelwelsh.com/posts/scalaz-monad-transformers)
