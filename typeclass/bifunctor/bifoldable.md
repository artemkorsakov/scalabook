# Bifoldable

Класс типов Foldable предназначен для структур, которые можно свернуть (_things that can be **folded** up_).

Операция _fold_ позволяет агрегировать.
Она берет начальный элемент и объединяет его с типом _Foldable_, следуя способу, предоставленному методом `f`.
_Fold_ может использоваться для реализации `reduce`.
Разница с `reduce` заключается в том,
что начальный элемент является либо идентификатором операции, указанной в `f`,
что означает элемент, который не изменяет значение,
например, пустая строка `""` для операции конкатенации строк или `0` для операции `+` в типе `Int`.
Можно реализовать версию `reduce`, в которой начальный элемент —
это просто первый элемент, который будет объединен в _fold_,
если есть доступ, например, к функции `head`.

Операции агрегации справа налево (`foldRight`), слева направо (`foldLeft`), используя моноид (`foldMap`),
свертывание `Foldable` (`combineAll`) и преобразование в связанный список (`toList`) можно выразить друг через друга.
Поэтому достаточно реализовать только `foldRight` или `foldMap`, но для лучшей производительности иногда
необходимо реализовать несколько операций напрямую.

Связь между операциями должна удовлетворять следующим законам:
- `foldLeft` соответствует `foldMap`: `fa.foldMap(Vector(_)) == fa.foldLeft(Vector.empty[A])(_ :+ _)`
- `foldRight` соответствует `foldMap`: `fa.foldMap(Vector(_)) == fa.foldRight(Vector.empty[A])(_ +: _)`


## Описание

```scala
trait Foldable[F[_]]:
  extension [A](fa: F[A])
    def foldRight[B](init: B)(f: (A, B) => B): B =
      fa.foldMap(f.curried)(using endoMonoid[B])(init)

    def foldLeft[B](acc: B)(f: (B, A) => B): B =
      fa.foldMap(a => b => f(b, a))(using dual(endoMonoid[B]))(acc)

    def foldMap[B](f: A => B)(using mb: Monoid[B]): B =
      fa.foldRight(mb.empty)((a, b) => mb.combine(f(a), b))

    def combineAll(using ma: Monoid[A]): A =
      fa.foldLeft(ma.empty)(ma.combine)

    def toList: List[A] =
      fa.foldRight(List.empty[A])(_ :: _)
```

## Примеры

### "Обертка"

```scala
case class Id[A](value: A)

given idFoldable: Foldable[Id] with
  extension [A](fa: Id[A])
    override def foldRight[B](init: B)(f: (A, B) => B): B =
      f(fa.value, init)
```

### [Option](../../scala/fp/functional-error-handling)

```scala
given Foldable[Option] with
  extension[A] (as: Option[A])
    override def foldRight[B](acc: B)(f: (A, B) => B) = as match
      case None => acc
      case Some(a) => f(a, acc)
    override def foldLeft[B](acc: B)(f: (B, A) => B) = as match
      case None => acc
      case Some(a) => f(acc, a)
    override def foldMap[B](f: A => B)(using mb: Monoid[B]): B =
      as match
        case None => mb.empty
        case Some(a) => f(a)
```

### [Последовательность](../../scala/collections)

```scala
given Foldable[List] with
  extension[A] (as: List[A])
    override def foldRight[B](acc: B)(f: (A, B) => B) =
      as.foldRight(acc)(f)
    override def foldLeft[B](acc: B)(f: (B, A) => B) =
      as.foldLeft(acc)(f)
    override def toList: List[A] = as
```

### [Кортеж](../../scala/collections/tuple) от двух и более элементов

```scala
given tuple2Foldable: Foldable[[X] =>> (X, X)] with
  extension [A](fa: (A, A))
    override def foldRight[B](init: B)(f: (A, B) => B): B =
      val (a0, a1) = fa
      val b = f(a1, init)
      f(a0, b)

given tuple3Foldable: Foldable[[X] =>> (X, X, X)] with
  extension [A](fa: (A, A, A))
    override def foldRight[B](init: B)(f: (A, B) => B): B =
      val (a0, a1, a2) = fa
      val b0 = f(a2, init)
      val b1 = f(a1, b0)
      f(a0, b1)
```

### [Either](../../fp/handling-errors)

```scala
given eitherFoldable[E]: Foldable[[x] =>> Either[E, x]] with
  extension [A](fa: Either[E, A])
    override def foldRight[B](init: B)(f: (A, B) => B): B =
      fa match
        case Right(a) => f(a, init)
        case Left(_)  => init
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fbifunctor%2FBifoldable.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fbifunctor%2FBifoldableSuite.scala&plain=1)


## Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._

List(1, 2, 3).foldRight (0) {_ - _}                        // 2
List(1, 2, 3).foldLeft (0) {_ - _}                         // -6

val trues: LazyList[Boolean] = LazyList.continually(true)
def lazyOr(x: Boolean)(y: => Boolean) = x || y
Foldable[LazyList].foldr(trues, false)(lazyOr)             // true

val digits = List(0,1,2,3,4,5,6,7,8,9)
Foldable[List].fold(digits)                                // 45
```


---

## References

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Bifoldable.html)
