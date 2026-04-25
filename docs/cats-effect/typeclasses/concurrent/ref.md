{%
showPrev = true
prevPage = ../concurrent.html
showNext = true
nextPage = deferred.html
%}

# Ref

Одновременная изменяемая ссылка.

```scala mdoc:silent
abstract class Ref[F[_], A] {
def get: F[A]
def set(a: A): F[Unit]
def updateAndGet(f: A => A): F[A]
def modify[B](f: A => (A, B)): F[B]
// ... и многое другое
}
```

Обеспечивает безопасный одновременный доступ и изменение своего содержимого, но не имеет функциональности для синхронизации, которая вместо этого обрабатывается [Deferred](deferred.md).

По этой причине `Ref` всегда инициализируется значением.

Реализация по умолчанию является неблокирующей и легковесной, состоящей
по сути из чисто функциональной обертки над `AtomicReference`.

Следовательно, его _нельзя_ использовать для хранения изменяемых данных, поскольку `AtomicReference#compareAndSet` и подобные методы зависят
от равенства ссылок на объекты.

### Счетчик параллельных операций

Это, вероятно, одно из наиболее распространенных применений этого примитива параллельного выполнения.

В этом примере рабочие процессы будут одновременно выполняться и обновлять значение `Ref`.

```scala mdoc:reset:silent
//> using lib "org.typelevel::cats-effect:3.7.0"

import cats.effect.{IO, IOApp, Sync}
import cats.effect.kernel.Ref
import cats.syntax.all._

class Worker[F[_]](id: Int, ref: Ref[F, Int])(implicit F: Sync[F]) {

private def putStrLn(value: String): F[Unit] =

F.blocking(println(value))

def start: F[Unit] =

for {
c1 <- ref.get

_ <- putStrLn(show"Worker #$id >> $c1")

c2 <- ref.updateAndGet(x => x + 1)

_ <- putStrLn(show"Worker #$id >> $c2")

} yield ()
}

object RefExample extends IOApp.Simple {

val run: IO[Unit] =

for {
ref <- Ref[IO].of(0)
w1 = new Worker[IO](1, ref)
w2 = new Worker[IO](2, ref)
w3 = new Worker[IO](3, ref)
_ <- List(
w1.start,
w2.start,
w3.start,
).parSequence.void

} yield ()
}
```

Это один из возможных результатов, показывающий “Worker #id » currentCount”:

```
Worker #1 >> 0
Worker #3 >> 0
Worker #2 >> 0
Worker #2 >> 3
Worker #1 >> 1
Worker #3 >> 2
```


---

**Ссылки:**

- [Cats effect documentation][doc]

[doc]: https://typelevel.org/cats-effect/docs/concepts
