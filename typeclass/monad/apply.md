# Apply

`Apply` реализует операцию `apply`
(также встречаются названия `join`, `sequence`, `joinWith`, `ap`, `applicate` - названия взаимозаменяемы),
которая объединяет `F[A]` и `F[A => B]` в `F[B]`.
`Apply` расширяет `Functor`.

Об `apply` можно думать как о своего рода усиленной `map`. 
В то время как `map` берет функцию и функтор и применяет функцию внутри значения функтора,
`apply` берет функтор, в котором есть функция, и другой функтор, 
извлекает эту функцию из первого функтора, а затем отображает ее на второй.

`Apply` должен удовлетворять следующим законам:
- Composition (композиция) - `apply(fbc)(apply(fab)(fa)) == apply(apply(fbc.map((bc: B => C) => (ab: A => B) => bc compose ab))(fab))(fa)`

### Примеры

##### Описание

```scala
trait InvariantFunctor[F[_]]:
  extension [A](fa: F[A]) def xmap[B](f: A => B, g: B => A): F[B]

trait Functor[F[_]] extends InvariantFunctor[F]:
  extension [A](fa: F[A])
    def map[B](f: A => B): F[B]
    
trait Apply[F[_]] extends Functor[F]:
  def apply[A, B](fab: F[A => B])(fa: F[A]): F[B]

  def apply2[A, B, C](fa: => F[A], fb: => F[B])(f: (A, B) => C): F[C] =
    apply(fa.map(f.curried))(fb)

  def tuple2[A, B](fa: => F[A], fb: => F[B]): F[(A, B)] =
    apply2(fa, fb)((_, _))

  def lift2[A, B, C](f: (A, B) => C): (F[A], F[B]) => F[C] =
    apply2(_, _)(f)
```

С помощью операций `apply` и `map` можно реализовать следующие операции:
- `apply2`, принимающую два `Apply` и функцию преобразования из типов этих `Apply` в третий тип `C` и возвращающую `Apply` от `C`. 
- `tuple2`, принимающую два `Apply` и возвращающую `Apply` от кортежа этих типов.
- `lift2`, "поднимающую" функцию `(A, B) => C` до функции преобразования `Apply`: `(F[A], F[B]) => F[C]`

Очевидно, эти операции можно дальше расширять на более длинные кортежи: `apply3`, `apply4` и т.д.

##### "Обертка"

```scala
case class Id[A](value: A)

given idApply: Apply[Id] with
  override def apply[A, B](fab: Id[A => B])(fa: Id[A]): Id[B] = Id(fab.value(fa.value))

  extension [A](as: Id[A]) override def map[B](f: A => B): Id[B] = Id(f(as.value))
```

##### [Option](../../scala/fp/functional-error-handling)

```scala
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

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonad%2FApply.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonad%2FApplySuite.scala)

### Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._

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

**References:**
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Apply.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Applicative.html)
