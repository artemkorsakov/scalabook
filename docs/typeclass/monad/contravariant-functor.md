# Контравариантный функтор

Контравариантный функтор (`F`) похож на [функтор](https://scalabook.gitflic.space/docs/typeclass/monad/functor), 
только с противоположной операцией `cmap`:

- `cmap(b: F[B])(f: A => B): F[A]`.

Контравариантный функтор создает новые экземпляры классов типов, добавляя функцию в начало цепочки преобразований,
в отличие от функтора, который добавляет её в конец.

Законы контравариантного функтора:

- Identity (тождественность): Если определен метод идентификации `id` такой, что: `id(a) == a`,
  тогда `cmap(fa)(id) == fa`.
- Composition (композиция): Если определены два метода `f: A => B` и `g: B => C`, тогда `cmap(cmap(fc)(g))(f) == cmap(fc)(g(f(_)))`.


## Описание

```scala
trait ContravariantFunctor[F[_]] extends InvariantFunctor[F]:
  self =>

  def cmap[A, B](b: F[B])(f: A => B): F[A]

  def contramap[A, B](b: F[B])(f: A => B): F[A] = cmap(b)(f)

  extension [A](fa: F[A]) override def xmap[B](f: A => B, g: B => A): F[B] = cmap(fa)(g)

  /** Композиция двух контравариантных функторов ковариантна */
  def compose[G[_]: ContravariantFunctor]: Functor[[X] =>> F[G[X]]] =
    new Functor[[X] =>> F[G[X]]]:
      private val g = summon[ContravariantFunctor[G]]
      extension [A](as: F[G[A]]) def map[B](f: A => B): F[G[B]] = cmap(as)(gb => g.cmap(gb)(f))

  /** Композиция контравариантного и ковариантного функторов контравариантна */
  def icompose[G[_]: Functor]: ContravariantFunctor[[X] =>> F[G[X]]] =
    new ContravariantFunctor[[X] =>> F[G[X]]]:
      private val g = summon[Functor[G]]
      def cmap[A, B](fa: F[G[B]])(f: A => B): F[G[A]] = self.cmap(fa)(g.lift(f))

  /** Произведение двух контравариантных функторов контравариантно */
  def product[G[_]: ContravariantFunctor]: ContravariantFunctor[[X] =>> (F[X], G[X])] =
    new ContravariantFunctor[[X] =>> (F[X], G[X])]:
      private val g = summon[ContravariantFunctor[G]]
      def cmap[A, B](fa: (F[B], G[B]))(f: A => B): (F[A], G[A]) =
        (self.contramap(fa._1)(f), g.contramap(fa._2)(f))
```

## Примеры

### Унарная функция

```scala
given functionContravariantFunctor[R]: ContravariantFunctor[[X] =>> Function1[X, R]] with
  def cmap[A, B](function: B => R)(f: A => B): A => R =
    a => function(f(a))
```


## Реализация в Cats

```scala
import cats.syntax.contravariant.*
import cats.Show

val showString = Show[String]
showString
  .contramap[Symbol](sym => s"'${sym.name}")
  .show(Symbol("dave"))
// val res0: String = 'dave
```


---

**Ссылки:**

- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FContravariantFunctor.scala&plain=1)
- [Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FContravariantFunctorSuite.scala)
- [Cats](https://typelevel.org/cats/typeclasses/contravariant.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp) 
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#contravariant)
- [Scalaz API](https://javadoc.io/static/org.scalaz/scalaz-core_3/7.3.6/scalaz/Contravariant.html)
