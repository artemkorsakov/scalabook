# Foldable

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


### Примеры Foldable

##### Описание Foldable

Пример агрегации справа налево.

```scala
trait Foldable[F[_]]:
  extension[A] (fa: F[A])
    def foldRight[B](init: B)(f: (A, B) => B): B
```

##### "Обертка"

```scala
case class Id[A](value: A)

given idFoldable: Foldable[Id] with
  extension [A](fa: Id[A])
    override def foldRight[B](init: B)(f: (A, B) => B): B =
      f(fa.value, init)
```

##### [Option](../../scala/fp/functional-error-handling)

```scala
given optionFoldable: Foldable[Option] with
  extension [A](fa: Option[A])
    override def foldRight[B](init: B)(f: (A, B) => B): B =
      fa match
        case Some(a) => f(a, init)
        case None    => init
```

##### [Последовательность](../../scala/collections)

```scala
given listFoldable: Foldable[List] with
  extension [A](as: List[A])
    override def foldRight[B](init: B)(f: (A, B) => B): B =
      as.foldRight(init)(f)
```

##### [Кортеж](../../scala/collections/tuple) от двух и более элементов

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

##### [Either](../../fp/handling-errors)

```scala
given eitherFoldable[E]: Foldable[[x] =>> Either[E, x]] with
  extension [A](fa: Either[E, A])
    override def foldRight[B](init: B)(f: (A, B) => B): B =
      fa match
        case Right(a) => f(a, init)
        case Left(_)  => init
```

### Реализации Foldable в различных библиотеках


---

**References:**
- [Tour of Scala](https://tourofscala.com/scala/foldable)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
