# Monad Transformer

Некоторые виды монад можно прокидывать внутрь других монад, тем самым трансформируя их.
Т.е. для этих видов монад (`F`) доступна операция `lift` - `M[A] -> M[F[A]]`

### Примеры трансформеров

##### Описание трансформера

```scala
trait MonadTransformer[M[_], T[_, _[_]]](using mMonad: Monad[M], tMonad: Monad[[X] =>> T[X, M]]):
  def lift[A](ma: M[A]): T[A, M]
```

##### "Обертка"

```scala
final case class IdT[A, F[_]](runIdT: F[Id[A]])

given idtMonad[M[_]](using outerMonad: Monad[M]): Monad[[X] =>> IdT[X, M]] with
  override def unit[A](a: => A): IdT[A, M] =
    IdT[A, M](outerMonad.unit(Id(a)))
  extension [A](fa: IdT[A, M])
    override def flatMap[B](f: A => IdT[B, M]): IdT[B, M] =
      IdT[B, M](outerMonad.flatMap(fa.runIdT)(ida => f(ida.value).runIdT))

given idtMonadTransformer[M[_]](using outerMonad: Monad[M]): MonadTransformer[M, IdT] with
  override def lift[A](a: M[A]): IdT[A, M] =
    IdT[A, M](outerMonad.map(a)(a => Id(a)))
```


[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FMonadTransformer.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FMonadTransformerSuite.scala)


### Реализации трансформеров в различных библиотеках


---

**References:**
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp) 
