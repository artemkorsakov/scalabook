{%
showPrev = true
prevPage = fiber_join.html
showNext = true
nextPage = concurrent.html
%}

# Spawn — создание и управление волокнами

`Spawn[F[_]]` — это класс типов, который предоставляет **легковесную абстракцию параллелизма** в виде [волокон (fibers)](../foundation/fibers.md). 
Это фундамент, на котором строятся все параллельные возможности Cats Effect.

```dotty
trait Spawn[F[_], E] extends MonadCancel[F, E]:
  def start[A](fa: F[A]): F[Fiber[F, E, A]]
  def never[A]: F[A]
  def cede: F[Unit]
  def racePair[A, B](fa: F[A], fb: F[B]): F[Either[
    (Outcome[F, E, A], Fiber[F, E, B]),
    (Fiber[F, E, A], Outcome[F, E, B])
  ]]
```

---

**Ссылки:**

- [Cats effect documentation][doc]

[doc]: https://typelevel.org/cats-effect/docs/concepts
