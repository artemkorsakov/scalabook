# Проверка типа

При сопоставлении с образцом есть две ситуации, когда необходимо выполнить проверку типа во время выполнения. 
Первый случай — это явная проверка типа с использованием нотации шаблона атрибуции.

```scala
(x: X) match
  case y: Y =>
```

Второй случай — когда экстрактор принимает аргумент, не являющийся подтипом контролируемого типа.

```scala
(x: X) match
  case y @ Y(n) =>

object Y:
  def unapply(x: Y): Some[Int] = ...
```

В обоих случаях проверка класса будет выполняться во время выполнения. 
Но когда проверка типа относится к абстрактному типу (параметру типа или члену типа), 
проверка не может быть выполнена, поскольку тип стирается во время выполнения.

Проверку типов в этих случаях можно выполнять если предоставлен [trait TypeTest](https://scala-lang.org/api/3.x/scala/reflect/TypeTest.html).

```scala
package scala.reflect

trait TypeTest[-S, T]:
  def unapply(s: S): Option[s.type & T]
```

Он предоставляет экстрактор, который возвращает свой аргумент, типизированный как `T`, если аргумент имеет тип `T`. 
Его можно использовать для кодирования проверки типа.

```scala
def f[X, Y](x: X)(using tt: TypeTest[X, Y]): Option[Y] = x match
  case tt(x @ Y(1)) => Some(x)
  case tt(x) => Some(x)
  case _ => None
```

Чтобы избежать синтаксических издержек, компилятор будет автоматически искать проверку типа, 
если обнаружит, что проверка типа относится к абстрактным типам. 
Это означает, что `x: Y` трансформируется в `tt(x)` и `x @ Y(_)` в `tt(x @ Y(_))`, 
если в области видимости доступен контекст `TypeTest[X, Y]`. 
Предыдущий код эквивалентен

```scala
def f[X, Y](x: X)(using TypeTest[X, Y]): Option[Y] = x match
  case x @ Y(1) => Some(x)
  case x: Y => Some(x)
  case _ => None
```

На стороне вызова можно было бы создать тест типа, 
где тест типа может быть выполнен непосредственно с тестами класса времени выполнения следующим образом.

```scala
val tt: TypeTest[Any, String] =
  new TypeTest[Any, String]:
    def unapply(s: Any): Option[s.type & String] = s match
      case s: String => Some(s)
      case _ => None

f[AnyRef, String]("acb")(using tt)
```

Компилятор синтезирует новый экземпляр теста типа, если ни один из них не найден в области видимости, например:

```scala
new TypeTest[A, B]:
  def unapply(s: A): Option[s.type & B] = s match
    case s: B => Some(s)
    case _ => None
```

Если проверки типов не могут быть выполнены, в тесте `case s: B =>` будет выдано непроверенное предупреждение.

Наиболее распространенными экземплярами `TypeTest` являются те, 
которые принимают любые параметры (т.е. `TypeTest[Any, T]`). 
Чтобы можно было использовать такие экземпляры непосредственно в границах контекста, предоставляется псевдоним

```scala
package scala.reflect

type Typeable[T] = TypeTest[Any, T]
```

Этот псевдоним можно использовать так:

```scala
import scala.reflect.Typeable

def f[T: Typeable]: Boolean =
  "abc" match
    case x: T => true
    case _ => false

f[String]
// res0: Boolean = true
f[Int]
// res1: Boolean = false
```

### Пример

Учитывая следующее абстрактное определение чисел Пеано, 
которое предоставляет два given экземпляра типов `TypeTest[Nat, Zero]` и `TypeTest[Nat, Succ]`:

```scala
import scala.reflect.*

trait Peano:
  type Nat
  type Zero <: Nat
  type Succ <: Nat

  def safeDiv(m: Nat, n: Succ): (Nat, Nat)

  val Zero: Zero

  val Succ: SuccExtractor
  trait SuccExtractor:
    def apply(nat: Nat): Succ
    def unapply(succ: Succ): Some[Nat]

  given typeTestOfZero: TypeTest[Nat, Zero]
  given typeTestOfSucc: TypeTest[Nat, Succ]
```

вместе с реализацией чисел Пеано на основе типа `Int`:

```scala
object PeanoInt extends Peano:
  type Nat  = Int
  type Zero = Int
  type Succ = Int

  def safeDiv(m: Nat, n: Succ): (Nat, Nat) = (m / n, m % n)

  val Zero: Zero = 0

  val Succ: SuccExtractor = new:
    def apply(nat: Nat): Succ = nat + 1
    def unapply(succ: Succ) = Some(succ - 1)

  def typeTestOfZero: TypeTest[Nat, Zero] = new:
    def unapply(x: Nat): Option[x.type & Zero] =
      if x == 0 then Some(x) else None

  def typeTestOfSucc: TypeTest[Nat, Succ] = new:
    def unapply(x: Nat): Option[x.type & Succ] =
      if x > 0 then Some(x) else None
```

можно написать следующую программу:

```scala
import PeanoInt.*

def divOpt(m: Nat, n: Nat): Option[(Nat, Nat)] =
  n match
    case Zero => None
    case s @ Succ(_) => Some(safeDiv(m, s))

val two = Succ(Succ(Zero))
// two: Int = 2
val five = Succ(Succ(Succ(two)))
// five: Int = 5

println(divOpt(five, two))
// Some((2,1))
println(divOpt(two, five))
// Some((0,2))
println(divOpt(two, Zero))
// None
```

Обратите внимание, что без `TypeTest[Nat, Succ]` паттерн `Succ.unapply(nat: Succ)` был бы `unchecked`.


---

**Ссылки:**

- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/other-new-features/type-test.html)
