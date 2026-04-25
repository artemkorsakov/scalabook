{%
showPrev = true
prevPage = ref.html
showNext = true
nextPage = ../temporal.html
%}

# Deferred

Чисто функциональный примитив синхронизации, представляющий собой единственное значение, которое может быть ещё недоступно.

При создании объект `Deferred` пуст. Затем его можно выполнить ровно один раз, и он никогда больше не станет пустым.

```scala mdoc:silent
abstract class Deferred[F[_], A] {
def get: F[A]

def complete(a: A): F[Boolean]
}
```

Ожидаемое поведение `get`

- `get` на пустом `Deferred` будет блокироваться до завершения `Deferred`
- `get` на завершенном `Deferred` всегда будет немедленно возвращать его содержимое
- `get` можно отменить, и при отмене он отпишется от зарегистрированного
  слушателя, операция, которая возможна до тех пор, пока значение `Deferred`
  не является полным

Ожидаемое поведение `complete`

- `complete(a)` на пустом `Deferred` установит его значение в `a`, уведомит всех читателей, заблокированных в данный момент при вызове `get`, и вернет `true`
- `complete(a)` на уже завершенном `Deferred` не будет изменять его содержимое и приведет к `false`

Несмотря на свою простоту, `Deferred` можно использовать в сочетании с `Ref` для построения сложного параллельного поведения и структур данных, таких как очереди и семафоры.

Наконец, упомянутая выше блокировка носит исключительно семантический характер, фактические потоки не блокируются реализацией.

### Только один раз

В ситуациях, когда множество процессов могут изменять одно и то же значение, но вас интересует только первый процесс, и вы останавливаете обработку, `Deferred[F, A]` — отличный пример его использования.

Два процесса попытаются завершиться одновременно, но преуспеет только один, завершив отложенный примитив ровно один раз. Проигравший получит `false` в `F` при попытке завершить уже завершенный `Deferred` или будет автоматически отменен механизмом `IO.race`.

```scala mdoc:reset:silent
import cats.effect.{IO, Deferred}
import cats.syntax.all._

def start(d: Deferred[IO, Int]): IO[Unit] = {
val attemptCompletion: Int => IO[Unit] = n => d.complete(n).void

List(
IO.race(attemptCompletion(1), attemptCompletion(2)),

d.get.flatMap { n => IO(println(show"Result: $n")) }

).parSequence.void
}

val program: IO[Unit] =

for {

d <- Deferred[IO, Int]

_ <- start(d)

} yield ()
```

---

**Ссылки:**

- [Cats effect documentation][doc]

[doc]: https://typelevel.org/cats-effect/docs/concepts

