# Monad Transformer

Некоторые виды монад можно прокидывать внутрь других монад, тем самым трансформируя их.
Т.е. для этих видов монад (`F`) доступна операция `lift` - `M[A] -> M[F[A]]`

## Описание

```scala
trait MonadTransformer[T[_[_], _], M[_]](using mMonad: Monad[M], tMonad: Monad[[X] =>> T[M, X]]):
  def lift[A](ma: M[A]): T[M, A]
```

## Примеры

### "Обертка"

```scala
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

### [Option](../../scala/fp/functional-error-handling)

```scala
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

### [Writer](../../fp/writer) - функциональный журнал

```scala
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

### [State](../../fp/state) - функциональное состояние

```scala
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

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FMonadTransformer.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FMonadTransformerSuite.scala)


---

## References

- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp) 
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monad+transformers.html)
