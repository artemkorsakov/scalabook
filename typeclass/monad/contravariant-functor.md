# Контравариантный функтор

Контравариантный функтор (`F`) похож на функтор, только с противоположной операцией `cmap`:
- `cmap(b: F[B])(f: A => B): F[A]`.

Законы контравариантного функтора:
- Identity (тождественность): Если определен метод идентификации `id` такой, что: `id(a) == a`,
  тогда `cmap(fa)(id) == fa`.
- Composition (композиция): Если определены два метода `f: A => B` и `g: B => C`, тогда `cmap(cmap(fc)(g))(f) == cmap(fc)(g(f(_)))`.


## Описание

```scala
trait ContravariantFunctor[F[_]]:
  def cmap[A, B](b: F[B])(f: A => B): F[A]
```

## Примеры

### Унарная функция является ковариантным функтором

```scala
given functionContravariantFunctor[R]: ContravariantFunctor[[X] =>> Function1[X, R]] with
  def cmap[A, B](function: B => R)(f: A => B): A => R =
    a => function(f(a))
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FContravariantFunctor.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FContravariantFunctorSuite.scala)


---

## References

- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp) 
