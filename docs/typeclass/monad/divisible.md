# Divisible

`Divisible` - контравариантный аналог [`Applicative`](https://scalabook.gitflic.space/docs/typeclass/monad/applicative). 
Он расширяет [`Divide`](https://scalabook.gitflic.space/docs/typeclass/monad/divide) 
и [`InvariantApplicative`](https://scalabook.gitflic.space/docs/typeclass/monad/invariant-applicative).

Законы `Divisible`:

- right identity: `divide(fa, conquer)(delta) == fa`, где `delta: A => (A, A) = a => (a, a)`
- left identity: `divide(conquer, fa)(delta) == fa`


## Описание

```scala
trait Divisible[F[_]] extends Divide[F], InvariantApplicative[F]:
  def conquer[A]: F[A]

  override def xunit0[A](a: => A): F[A] = conquer

  override def cmap[A, B](fb: F[B])(f: A => B): F[A] =
    divide(conquer[Unit], fb)(c => ((), f(c)))
```

## Примеры

### Унарная функция

```scala
given functionDivisible[R: Monoid]: Divisible[[X] =>> Function1[X, R]] with
  override def divide[A, B, C](fa: => A => R, fb: => B => R)(f: C => (A, B)): C => R =
    c => {
      val (a, b) = f(c)
      summon[Monoid[R]].combine(fa(a), fb(b))
    }

  override def conquer[A]: A => R = _ => summon[Monoid[R]].empty
```


---

**Ссылки:**

- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FDivisible.scala&plain=1)
- [Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FDivisibleSuite.scala)
- [Scalaz API](https://javadoc.io/static/org.scalaz/scalaz-core_3/7.3.6/scalaz/Divisible.html)
