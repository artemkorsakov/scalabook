# Apply

**Apply** — это абстракция в функциональном программировании, которая расширяет возможности [`Semigroupal`](semigroupal.md), 
добавляя возможность применять функцию, находящуюся в контексте, к значению, также находящемуся в контексте. 
`Apply` является менее строгой версией [`Applicative`](applicative.md), 
так как не требует наличия операции `pure` (которая помещает значение в контекст).

**Основная идея Apply**

`Apply` — это тип, который реализует две основные операции:

- `product` (или `tuple2`, `tuple3` и т.д.) — комбинирует два значения в контексте в пару (или кортеж) в том же контексте.
- `ap` (или `apply`) — применяет функцию в контексте к значению в контексте.

**Зачем нужен Apply?**

`Apply` полезен, когда вам нужно:

- Применять функции, находящиеся в контексте, к значениям, также находящимся в контексте.
- Комбинировать несколько значений в контексте (например, результаты нескольких асинхронных вычислений или несколько опциональных значений).
- Работать с эффектами (например, асинхронность, опциональность, ошибки) в композируемом стиле, 
  но без необходимости использовать более строгую абстракцию `Applicative`.

**Примеры Apply**

- `Option` как `Apply`. `Option` — это контейнер, который может содержать значение (`Some`) или быть пустым (`None`). 
  `Apply` позволяет применять функцию к нескольким опциональным значениям.
- `List` как `Apply`. `List` — это контейнер, содержащий множество значений. 
  `Apply` позволяет применять функцию ко всем возможным комбинациям элементов из нескольких списков.
- `Future` как `Apply`. `Future` — это контейнер, представляющий асинхронное вычисление. 
  `Apply` позволяет комбинировать несколько асинхронных вычислений.

## Формальное определение

`Apply` реализует операцию `apply`
(также встречаются названия `join`, `sequence`, `joinWith`, `ap`, `applicate` - названия взаимозаменяемы),
которая объединяет `F[A]` и `F[A => B]` в `F[B]`.
`Apply` расширяет [`Functor`](functor.md) и [`Semigroupal`](semigroupal.md).

Об `apply` можно думать как о своего рода усиленной `map`. 
В то время как `map` берет функцию и функтор и применяет функцию внутри значения функтора,
`apply` берет функтор, в котором есть функция, и другой функтор, 
извлекает эту функцию из первого функтора, а затем отображает ее на второй.

`Apply` должен удовлетворять следующим законам:

- Композиция (composition) - `apply(fbc)(apply(fab)(fa)) == apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa)`

## Код

```dotty
trait Apply[F[_]] extends Functor[F], Semigroupal[F]:
  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  def apply2[A, B, C](fa: => F[A], fb: => F[B])(f: (A, B) => C): F[C] =
    apply(fa.map(f.curried))(fb)

  override def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
    apply2(fa, fb)((_, _))

  def lift2[A, B, C](f: (A, B) => C): (F[A], F[B]) => F[C] =
    apply2(_, _)(f)
```

С помощью операций `apply` и `map` можно реализовать следующие операции:

- `apply2`, принимающую два `Apply` и функцию преобразования из типов этих `Apply` в третий тип `C` и возвращающую `Apply` от `C`. 
- `tuple2`, принимающую два `Apply` и возвращающую `Apply` от кортежа этих типов.
- `lift2`, "поднимающую" функцию `(A, B) => C` до функции преобразования `Apply`: `(F[A], F[B]) => F[C]`

Очевидно, эти операции можно дальше расширять на более длинные кортежи: `apply3`, `apply4` и т.д.

### "Обертка"

```dotty
type Id[A] = A
object Id:
  def apply[A](a: A): Id[A] = a

given Apply[Id] with
  override def apply[A, B](fab: Id[A => B])(fa: Id[A]): Id[B] = Id(fab(fa))

  extension [A](as: Id[A]) override def map[B](f: A => B): Id[B] = Id(f(as))
```

Докажем, что обертка удовлетворяет закону `Apply`: 
`apply(fbc)(apply(fab)(fa)) == apply(apply(fbc.map(bc => ab => bc compose ab))(fab))(fa)`

- `apply(fbc)(apply(fab)(fa)) = apply(fbc)(fab(fa)) = fbc(fab(fa)) = (bc compose ab)(fa)`
- ```dotty
  apply(apply(fbc.map(bc => ab => bc compose ab))(fab))(fa) = 
  fbc.map(bc => ab => bc compose ab)(fab)(fa) = 
  f(ab => bc compose ab)(fab)(fa) = (bc compose ab)(fa)
  ```

### Option

```dotty
given Apply[Option] with
  override def apply[A, B](fab: Option[A => B])(fa: Option[A]): Option[B] =
    (fab, fa) match
      case (Some(aToB), Some(a)) => Some(aToB(a))
      case _                     => None

  extension [A](as: Option[A])
    override def map[B](f: A => B): Option[B] =
      as match
        case Some(a) => Some(f(a))
        case None    => None
```

### Законы

- Закон `Apply` гласит, что композиция функций внутри аппликативного функтора должна быть согласована 
  с применением функций по отдельности. Формально это можно выразить так:
  `apply(fbc)(apply(fab)(fa)) == apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa)`

```dotty
... // Законы Функтора и Semigroupal

def checkApplyComposition[F[_], A, B, C](
    fa: F[A],
    fab: F[A => B],
    fbc: F[B => C]
)(using ap: Apply[F]): ValidatedNel[String, Unit] =
  import ap.apply
  Either.cond[String, Unit](
    apply(fbc)(apply(fab)(fa)) ==
      apply(
        apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab)
      )(fa),
    (),
    "Не соблюдается композиция: apply(fbc)(apply(fab)(fa)) == apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa)"
  ).toValidatedNel
```

## Схема

```mermaid
classDiagram
    class InvariantFunctor~A~{
        +xmap[B](fa: F[A], f: A => B, g: B => A) F[B]
    }
    class Semigroupal~A~{
        +product[A, B](fa: F[A], fb: F[B]) F[Tuple2[A, B]]
    }
    class Functor~A~{
        +map[B](fa: F[A], f: A => B) F[B]
    }
    InvariantFunctor <|-- Functor    
    class Apply~A~{
        +apply[A, B](fab: F[A => B])(fa: F[A]) F[B]
    }
    Functor <|-- Apply
    Semigroupal <|-- Apply
```

## Реализация в библиотеках

### Реализация в Cats

```dotty
import cats.*, cats.data.*, cats.syntax.all.*

Apply[Option].ap({{(_: Int) + 3}.some })(10.some)               // Some(13)
Apply[Option].ap({{(_: String) + "hahah"}.some })(none[String]) // None
Apply[Option].ap({ none[String => String] })("woot".some)       // None
```

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*

// Наследуются операции от функтора
...

val intToString: Int => String = _.toString
val double: Int => Int = _ * 2
val addTwo: Int => Int = _ + 2
Apply[Option].ap(1.some)(some(intToString))           // Some(1)
Apply[Option].ap(none)(some(double))                  // None
Apply[List].ap(List(1,2,3))(List(double, addTwo))     // List(2, 4, 6, 3, 4, 5)

val add2 = ((_:Int) + (_:Int))
Apply[Option].apply2(some(1), some(2))(add2)          // Some(3)

Apply[List].tuple2(List(1,2,3), List("a", "b"))       // List((1,a), (1,b), (2,a), (2,b), (3,a), (3,b))

Apply[List].lift2 {(_: Int) * (_: Int)} (List(1, 2), List(3, 4)) // List(3, 4, 6, 8)

1.some <*> {(_: Int) + 2}.some                                   // Some(3)
1.some <*> { 2.some <*> {(_: Int) + (_: Int)}.curried.some }     // Some(3)
1.some <* 2.some                                                 // Some(1)
1.some *> 2.some                                                 // Some(2)
(3.some |@| 5.some) {_ + _}                                      // Some(8)
```

---

**Ссылки:**

- [Herding Cats](http://eed3si9n.com/herding-cats/Apply.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Applicative.html)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#apply-and-applicative)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Apply.html)
