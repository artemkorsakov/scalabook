# Divide

`Divide` - контравариантный аналог [`Apply`](apply). Он расширяет ковариантный функтор и дополнительно реализует
операцию `divide`: `def divide[A, B, C](fa: => F[A], fb: => F[B])(f: C => (A, B)): F[C]`

Законы `Divide`:
- Composition (композиция): `divide(divide(fa1, fa2)(delta), fa3)(delta) == divide(fa1, divide(fa2, fa3)(delta))(delta)`,
  где `delta: A => (A, A) = a => (a, a)`


## Описание

```scala
trait Divide[F[_]] extends ContravariantFunctor[F]:
  def divide[A, B, C](fa: => F[A], fb: => F[B])(f: C => (A, B)): F[C]

  def tuple2[A, B](fa: => F[A], fb: => F[B]): F[(A, B)] = divide(fa, fb)(identity)
```

## Примеры

### Унарная функция

```scala
given functionDivide[R: Monoid]: Divide[[X] =>> Function1[X, R]] with
  override def cmap[A, B](function: B => R)(f: A => B): A => R =
    a => function(f(a))

  override def divide[A, B, C](fa: => A => R, fb: => B => R)(f: C => (A, B)): C => R =
    c => {
      val (a, b) = f(c)
      summon[Monoid[R]].combine(fa(a), fb(b))
    }
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FDivide.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FDivideSuite.scala)


---

**Ссылки:**

- [Scalaz API](https://javadoc.io/static/org.scalaz/scalaz-core_3/7.3.6/scalaz/Divide.html)
