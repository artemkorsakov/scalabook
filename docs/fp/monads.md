# Monads

## Functor

[Функтор](https://scalabook.gitflic.space/docs/typeclass/monad/functor) — это преобразование из категории `A` в категорию `B`.
Такие преобразования часто изображаются стрелкой: `A -> B` (или через метод `map`).

Функтор можно описать с помощью `trait`:

```scala
trait Functor[F[_]]:
  extension [A](fa: F[A])
    def map[B](f: A => B): F[B]
```

### Законы функтора

Функтор должен следовать нескольким законам:

- Identity (тождественность): `x.map(a => a) == x`.
- Composition (композиция): `x.map(f).map(g) == x.map(g(f(_)))`.

### Свойства функтора

Функтор `F[(A, B)]` можно «распределить» по паре, чтобы получить `(F[A], F[B])`:

```scala
trait Functor[F[_]]:
  ...
  extension [A, B](fab: F[(A, B)])
    def distribute: (F[A], F[B]) =
      (fab.map(_(0)), fab.map(_(1)))
```

Эта операция иногда называется _unzip_.
Эта универсальная функция распаковки работает с любым функтором! 


## Applicative

[Applicative](https://scalabook.gitflic.space/docs/typeclass/monad/applicative) расширяет `Functor` и позволяет работать с несколькими «ящиками».
В аппликативных функторах примитивами являются `unit` и `map2`.

```scala
trait Applicative[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  extension [A](fa: F[A])
    def map2[B,C](fb: F[B])(f: (A, B) => C): F[C]

    def map[B](f: A => B): F[B] =
      fa.map2(unit(()))((a, _) => f(a))
```

`map` можно реализовать с помощью `unit` и `map2`, что говорит о том, что все `Applicative` являются функторами.

### Законы Applicative

Для `Applicative` должны соблюдаться следующие законы:
- `map(apply(x))(f) == apply(f(x))`
- `map2(apply(x), apply(y)) == apply((x, y))`

### Виды Applicative

#### Applicative с `unit` и `map2`

`apply` и `map` могут быть выражены так:

```scala
trait Applicative[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  extension [A](fa: F[A])
    def map2[B,C](fb: F[B])(f: (A, B) => C): F[C]

    def map[B](f: A => B): F[B] =
      fa.map2(unit(()))((a, _) => f(a))

  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B] =
    fab.map2(fa)((f, a) => f(a))
```

#### Applicative с `unit` и `apply`

`map2` и `map` могут быть выражены так:

```scala
trait Applicative[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  extension [A](fa: F[A])
    def map2[B,C](fb: F[B])(f: (A, B) => C): F[C] =
      apply(apply(unit(f.curried))(fa))(fb)

    def map[B](f: A => B): F[B] =
      apply(unit(f))(fa)
```

### Комбинаторы Applicative

`Applicative` определяет некоторые комбинаторы:

```scala
def sequence[A](fas: List[F[A]]): F[List[A]] =
  traverse(fas)(identity)

def traverse[A,B](as: List[A])(f: A => F[B]): F[List[B]] =
  as.foldRight(unit(List[B]()))((a, acc) => f(a).map2(acc)(_ :: _))

def replicateM[A](n: Int, fa: F[A]): F[List[A]] =
  sequence(List.fill(n)(fa))

def product[A, B](fa: F[A], fb: F[A]): F[(A,B)] =
  fa.map2(fb)((a, b) => (a, b))
```


## Monad

[Монада](https://scalabook.gitflic.space/docs/typeclass/monad/monad) - 
это [Applicative](https://scalabook.gitflic.space/docs/typeclass/monad/applicative) 
(а значит и [Functor](https://scalabook.gitflic.space/docs/typeclass/monad/functor))
с дополнительной функцией: `flatten` (сведение: `F[F[A]] -> F[A]`).
Что позволяет определить `flatMap` — `map`, за которой следует `flatten`.

### Законы монады

Для `Monad` должны соблюдаться следующие законы (помимо законов Функтора):
- identities:
    - `flatMap(apply(x))(fn) == fn(x)`
    - `flatMap(m)(apply _) == m`
- associativity на flatMap:
    - `flatMap(flatMap(m)(f))(g) == flatMap(m) { x => flatMap(f(x))(g) }`

### Виды монад

#### Monad с `flatMap` и `unit`

Монада может быть определена с помощью `flatMap` и `unit`.
В этом случае `map` и `map2` будут определяться так:

```scala
trait Monad[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  extension [A](fa: F[A])
    def flatMap[B](f: A => F[B]): F[B]

    def map[B](f: A => B): F[B] =
      flatMap(a => unit(f(a)))

    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      fa.flatMap(a => fb.map(b => f(a, b)))
```

#### Monad с `compose` и `unit`

Монада может быть определена с помощью `compose` и `unit`.
В этом случае `flatMap` (и через неё остальные операции) будет определяться так:

```scala
trait Monad[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  def compose[A, B, C](f: A => F[B], g: B => F[C]): A => F[C]

  extension [A](fa: F[A])
    def flatMapViaCompose[B](f: A => F[B]): F[B] =
      compose[Unit, A, B](_ => fa, f)(())
```

В этом случае законы Монады примут вид:
- identities:
  - `compose(f, unit) == f`
  - `compose(unit, f) == f`
- associativity:
  - `compose(compose(f, g), h) == compose(f, compose(g, h))`

#### Monad с `map`, `join` и `unit`

Монада может быть определена с помощью `map`, `join` и `unit`.
В этом случае `flatMap` и `compose` могут быть определены так:

```scala
trait Functor[F[_]]:
  extension [A](fa: F[A]) 
    def map[B](f: A => B): F[B]

trait Monad[F[_]] extends Functor[F]:
  def unit[A](a: => A): F[A]

  extension[A] (ffa: F[F[A]])
    def join: F[A]

  extension [A](fa: F[A])
    def flatMapViaJoinAndMap[B](f: A => F[B]): F[B] =
      fa.map(f).join
 
  def composeViaJoinAndMap[A, B, C](f: A => F[B], g: B => F[C]): A => F[C] =
    a => f(a).map(g).join      
```

### Примеры монад

#### Option

```scala
given optionMonad: Monad[Option] with
  def unit[A](a: => A): Option[A] = Some(a)

  extension[A] (fa: Option[A])
    override def flatMap[B](f: A => Option[B]): Option[B] = fa match
      case Some(a) => f(a)
      case None => None
```

#### List

```scala
given listMonad: Monad[List] with
  def unit[A](a: => A): List[A] = a :: Nil
  
  extension [A](fa: List[A])
    override def flatMap[B](f: A => List[B]) =
      fa.flatMap(f)
```

### Комбинаторы монад

Монада определяет некоторые комбинаторы:

```scala
def sequence[A](fas: List[F[A]]): F[List[A]] =
  traverse(fas)(identity)

def traverse[A, B](as: List[A])(f: A => F[B]): F[List[B]] =
  as.foldRight(unit(List.empty[B]))((a, acc) => f(a).map2(acc)(_ :: _))

def replicateM[A](n: Int, fa: F[A]): F[List[A]] =
  sequence(List.fill(n)(fa))

// Kleisli arrows
def compose[A, B, C](f: A => F[B], g: B => F[C]): A => F[C] =
  a => f(a).flatMap(g)  
```


---

**Ссылки:**

- [Functional Programming in Scala, Second Edition, Chapter 11](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
