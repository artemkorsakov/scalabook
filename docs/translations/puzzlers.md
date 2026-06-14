{%
showPrev = true
prevPage = type-level-programming-in-scala.html
showNext = true
nextPage = project_euler.html
%}

# Головоломки на Scala

В этом разделе приведены различные головоломки, 
взятые из книги [Scala Puzzlers][Scala Puzzlers].

## Hi There!

```dotty
List(1, 2).map { i => i + 1 }
// res0: List[Int] = List(2, 3)
List(1, 2).map { _ + 1 }
// res1: List[Int] = List(2, 3)
List(1, 2).map { i => println("Hi"); i + 1 }
// Hi
// Hi
// res2: List[Int] = List(2, 3)
List(1, 2).map { println("Hi"); _ + 1 }
// Hi
// res3: List[Int] = List(2, 3)
```

Несмотря на то, что упрощение `_` выглядит так же, во втором случае оно имеет совсем другой эффект:
оператор `println` больше не является частью тела функции!
Вместо этого выражение оценивается при определении функции, которую необходимо передать `map` —
как и во всех блоках, возвращается последнее значение.

## UPSTAIRS downstairs

```dotty
val ij: (Int, Int) = (3, 4)
// ij: Tuple2[Int, Int] = (3, 4)
val (i, j): (Int, Int) = (3, 4)
// i: Int = 3
// j: Int = 4
val IJ: (Int, Int) = (3, 4)
// IJ: Tuple2[Int, Int] = (3, 4)
```

```dotty
val (I, J): (Int, Int) = (3, 4)
val (`i`, `j`): (Int, Int) = (3, 4)
// error:
// Not found: I
// val (I, J): (Int, Int) = (3, 4)
//      ^
// error:
// Not found: J
// val (I, J): (Int, Int) = (3, 4)
//         ^
// error:
// Not found: i
// val (`i`, `j`): (Int, Int) = (3, 4)
//      ^^^
// error:
// Not found: j
// val (`i`, `j`): (Int, Int) = (3, 4)
//           ^^^
```

Первые три случая - это присвоение переменным значений кортежа.
Последние два пытаются сопоставить `(3, 4)` с константами `I` и `J`
или переменными `i` и `j`, которые должны быть определены ранее —
Scala полагает, что переменные ВЕРХНЕГО РЕГИСТРА в сопоставлении с шаблоном являются константами.

## Location, Location, Location

```dotty
trait A:
  val audience: String
  println(s"Hello $audience")

class BMember(a: String = "World") extends A:
  val audience = a
  println(s"I repeat: Hello $audience")

class BConstructor(val audience: String = "World") extends A:
  println(s"I repeat: Hello $audience")

BMember("Readers")
// Hello null
// I repeat: Hello Readers
BConstructor("Readers")
// Hello Readers
// I repeat: Hello Readers
```

При создании экземпляров суперклассов или трейтов
родительский конструктор можно визуализировать как выполняемый
перед первой строкой дочернего конструктора, но после определения класса.

## Now You See Me, Now You Don't

```dotty
trait A:
  val foo: Int
  val bar = 10
  println(s"In A: foo: $foo, bar: $bar")

class B extends A:
  val foo: Int = 25
  println(s"In B: foo: $foo, bar: $bar")

class C extends B:
  override val bar = 99
  println(s"In C: foo: $foo, bar: $bar")

new C
// In A: foo: 0, bar: 0
// In B: foo: 25, bar: 0
// In C: foo: 25, bar: 99
```

Обратите внимание, что `bar` — это `val`, переопределяемый в `C`.
Компилятор Scala инициализирует `val`-ы только один раз,
поэтому, поскольку `bar` будет инициализирован в `C`,
он не инициализируется до этого времени
и отображается как значение по умолчанию (в данном случае `0`) во время строительства суперкласса.

## The Missing List

```dotty
def sumSizes(collections: Iterable[Iterable[_]]): Int = 
  collections.map(_.iterator.size).sum

sumSizes(List(Set(1, 2), List(3, 4)))
// res4: Int = 4
sumSizes(Set(List(1, 2), Set(3, 4)))
// res5: Int = 2
```

Несмотря на то, что `collections.map` может сопоставлять итерируемый объект с другим «хорошим» итерируемым объектом,
поскольку при редизайне коллекций тип возвращаемого итерируемого объекта
будет (обычно) соответствовать типу ввода.
Что для `Set` означает... никаких дубликатов.
И да, `foldLeft`, очевидно, был бы гораздо лучшим способом сделать это.

## Arg Arrgh!

```dotty
def square[T : Numeric](n: T) = summon[Numeric[T]].times(n, n)

def twiceA[T](f: T => T, a: T) = f(f(a))
def twiceB[T](f: T => T)(a: T) = f(f(a))
def twiceC[T](a: T, f: T => T) = f(f(a))
def twiceD[T](a: T)(f: T => T) = f(f(a))
```

```dotty
twiceA(square, 2)
// error: 
// No implicit Ordering defined for Any.
twiceB(square)(2)
// error: 
// No implicit Ordering defined for Any.
```

```dotty
twiceC(2, square)
// res7: Int = 16
twiceD(2)(square)
// res8: Int = 16
```

Чтобы `square` можно было использовать в качестве аргумента,
компилятор должен знать, что `T` привязан к (типу, который может быть неявно преобразован) к `Numeric`.
Несмотря на то, что может показаться, что `2` как `Int` явно удовлетворяет этому условию,
эта информация недоступна для компилятора до появления `2` в качестве аргумента.
Только в последних двух версиях `T` связывается «достаточно рано», чтобы `square` был разрешен.

## Captured by Closures

```dotty
val funcs1 = collection.mutable.Buffer[() => Int]()
val funcs2 = collection.mutable.Buffer[() => Int]()

{
  val values = Seq(100, 110, 120)
  var j = 0
  for i <- values.indices
  do  
    funcs1 += (() => values(i))
    funcs2 += (() => values(j))
    j += 1
}
```

```dotty
funcs1 foreach { f1 => println(f1()) }
```

```dotty
funcs2 foreach { f2 => println(f2()) }
// java.lang.IndexOutOfBoundsException: 3
// 	at scala.collection.LinearSeqOps.apply(LinearSeq.scala:117)
//  ...
```

Когда `var` используется вместо `val`, функции закрываются по переменной, а не по значению.
Поскольку `i` определен в `for-comprehension`, он определяется как `val`.
Это означает, что каждый раз, когда `i` сохраняется где-то, его значение копируется,
поэтому печатается ожидаемый результат:

```
100
110
120
```

При изменении `j` внутри цикла все три замыкания «видят» одну и ту же переменную `j`, а не ее копию.
Поэтому после окончания цикла, когда `j` равно 3, выдается исключение `IndexOutOfBoundsException`.

Исключения можно избежать, «зафиксировав» возвращаемое значение:

```dotty
val funcs1 = collection.mutable.Buffer[() => Int]()
val funcs2 = collection.mutable.Buffer[() => Int]()

{
  val values = Seq(100, 110, 120)
  var j = 0
  for i <- values.indices
  do
    funcs1 += (() => values(i))
    val value = values(j)
    funcs2 += (() => value)
    j += 1
}
```

```dotty
funcs1 foreach { f1 => println(f1()) }
// 100
// 110
// 120
funcs2 foreach { f2 => println(f2()) }
// 100
// 110
// 120
```

Присвоение значения выполняется немедленно и, таким образом, «захватывает» предполагаемый элемент значений.

## Init You, Init Me

```dotty
object XY:
  object X:
    val value: Int = Y.value + 1
  object Y:
    val value: Int = X.value + 1

println(if math.random > 0.5 then XY.X.value else XY.Y.value)
// 2
```

Рекурсивное определение двух полей `X.value` и `Y.value` допустимо и компилируется,
но поскольку оно является рекурсивным, тип (`Int`) должен быть указан как минимум для `X.value`.
При доступе к объекту `X` или `Y` значение его поля инициализируется
(объекты не инициализируются до тех пор, пока к ним не будет осуществлен доступ).
Если сначала инициализируется объект `X`, то инициализация значения его поля запускает инициализацию объекта `Y`.
Чтобы инициализировать поле `Y.value`, осуществляется доступ к полю `X.value`.
Виртуальная машина замечает, что инициализация объекта `X` уже запущена,
и возвращает текущее значение `X.value`, равное нулю (значение по умолчанию для полей `Int`),
поэтому во время выполнения не происходит переполнения стека.
Как следствие, `Y.value` устанавливается равным `0 + 1 = 1`, а `X.value` — равным `1 + 1 = 2`.
Однако, если сначала инициализируется объект `Y`, тогда `Y.value` инициализируется `2`, а `X.value` — `1`.
В операторе `if` мы получаем доступ либо к `X.value`, либо к `Y.value`
и принудительно инициализируем либо `X`, либо `Y`.
Но, как мы видели выше, к какому бы объекту ни обращались первым, значение его поля инициализируется равным `2`,
поэтому результат условного оператора всегда `2`.

## A Case of Equality

```dotty
trait SpyOnEquals:
  override def equals(x: Any) =
    println("DEBUG: In equals")
    super.equals(x)

case class CC()
case class CCSpy() extends SpyOnEquals

val cc1 = new CC() with SpyOnEquals
val cc2 = new CC() with SpyOnEquals
val ccspy1 = CCSpy()
val ccspy2 = CCSpy()

println(cc1 == cc2)
// DEBUG: In equals
// true
println(cc1.## == cc2.##)
// true
println(ccspy1 == ccspy2)
// DEBUG: In equals
// false
println(ccspy1.## == ccspy2.##)
// true
```

Для case классов генерируются методы `equals`, `hashCode` и `toString`,
и для двух экземпляров case класса с одинаковыми элементами можно ожидать,
что и `equals`, и `hashCode` вернут один и тот же результат.
Однако в соответствии с SLS (§5.3.2) case класс неявно переопределяет методы
`equals`, `hashCode` и `toString` класса `scala.AnyRef` только в том случае,
если сам case класс не предоставляет определение для одного из этих методов
и только если конкретное определение не дано в каком-либо базовом классе case класса (кроме `AnyRef`).
В нашем примере у `CCSpy` базовый trait `SpyOnEquals` предоставляет метод `equals`,
поэтому case класс не предоставляет собственного определения,
и сравнение двух разных экземпляров возвращает `false`.
Однако для метода `hashCode` не предусмотрено никакой реализации ни в классе `CCSpy`,
ни в каком-либо базовом классе,
поэтому здесь используется неявно переопределенная версия,
которая возвращает одно и то же значение для равных элементов.
Для case класса `CC` определение `equals` или `hashCode` не предоставляется,
поэтому для обоих методов используются неявно переопределенные версии.
Смешивание с `SpyOnEquals` при создании экземпляров классов на это не влияет.

## If At First You Don't Succeed...

```dotty
var x = 0
lazy val y = 1 / x

try println(y)
catch
  case _: Throwable =>
    x = 1
    println(y)
// 1    
```

Одна из самых интересных особенностей ленивых значений (кроме того, что они откладывают фактическое вычисление)
заключается в том, что они будут пересчитываться при вызове,
если в момент первого доступа возникло исключение,
до тех пор, пока не будет получено какое-то определенное значение.
Таким образом, можно использовать этот полезный шаблон во многих ситуациях,
например, для обработки отсутствующих файлов.

## To Map, or Not to Map

```dotty
case class RomanNumeral(symbol: String, value: Int)

given Ordering[RomanNumeral] with
  def compare(a: RomanNumeral, b: RomanNumeral) = a.value compare b.value

val numerals = collection.immutable.SortedSet(
  RomanNumeral("M", 1000),
  RomanNumeral("C", 100),
  RomanNumeral("X", 10),
  RomanNumeral("I", 1),
  RomanNumeral("D", 500),
  RomanNumeral("L", 50),
  RomanNumeral("V", 5)
)

println("Roman numeral symbols for 1 5 10 50 100 500 1000:")
// Roman numeral symbols for 1 5 10 50 100 500 1000:
for (num <- numerals; sym = num.symbol) do print(s"$sym ")
// I V X L C D M 
numerals map (_.symbol) foreach (sym => print(s"$sym "))
// C D I L M V X
```

Поскольку `RomanNumerals` упорядочены по их значениям,
итерация по отсортированному набору цифр вернет их в этом порядке
и приведет к печати представлений в соответствии со значением.
Сопоставление чисел с их символами приведет к отсортированному набору строк,
которые, естественно, будут упорядочены лексикографически.
Таким образом, итерация по ним вернет их в этом порядке.

Потенциально неожиданное переупорядочение элементов в итерируемом объекте можно было бы предотвратить,
например, с помощью

```dotty
numerals.view map { _.symbol } foreach (sym => print(s"$sym "))
// I V X L C D M
```

или

```dotty
numerals.toSeq map { _.symbol } foreach (sym => print(s"$sym "))
// I V X L C D M
```

Обратите внимание, что первоначальный порядок объявления набора не влияет на порядок итерации.
Также обратите внимание, что пример кода, использующий `map`, неэффективно перебирает числа дважды.


```dotty
numerals foreach { num => print(s"${num.symbol} ") }
// I V X L C D M
```

является более эффективным и выводит значения в ожидаемом порядке.

## Private Lives

```dotty
object Lives:
  class Private:
    def foo1: Any = new Private.C1
    def foo2: Any = new Private.C2

  object Private:
    class C1 private {}
    private class C2 {}
// error:
// constructor C1 cannot be accessed as a member of repl.MdocSession.App.Lives.Private.C1 from class Private.
//     def foo1: Any = new Private.C1
//                         ^^^^^^^^^^
```

Сопутствующий объект `Private` определяет два класса `C1` и `C2`.
`C2` — это приватный класс, который доступен только в объекте `Private` и его сопутствующем классе,
тогда как `C1` — это общедоступный класс с приватным конструктором по умолчанию,
поэтому этот класс может быть создан только в классе `C1`.
Как следствие, метод `foo2` компилируется нормально (поскольку приватный класс `C2` виден в классе `Private`),
тогда как реализация метода `foo1` сообщает об ошибке компилятора.
Обратите внимание, что тип результата метода `foo2` должен быть установлен на базовый тип `Private.C2`,
иначе тип результата этого метода будет невидимым для любого вызывающего объекта
за пределами класса/объекта `Private`
(компилятор сообщит что закрытый класс `C2` выходит за рамки своей определяющей области).
Для метода `foo1` в этом нет необходимости.

## Self - See 'Self'

```dotty
val s1: String = s1
val s2: String = s2 + s2
println(s1.length) // NullPointerException
println(s2.length) // 8
```

Определения значений `s1` и `s2` действительны и компилируются без ошибок.
Поскольку определения являются рекурсивными, необходимо указать тип объявленных значений (SLS §4.1).
В Scala поля объекта предварительно инициализируются до значения по умолчанию,
а для типа `String` (и всех других ссылочных типов) значение по умолчанию равно `null`.
Это как в Java.

Однако такое рекурсивное определение поля в теле класса Java дает недопустимую ошибку компилятора прямой ссылки:

```java
class Z {
   String s = s + s;
}
```

, но следующее компилируется нормально:

```java
class Z {
    String s = this.s + this.s;
}
```

Это показывает, что проверка, которую выполняет компилятор Java, довольно поверхностна.

Способ Scala принимать рекурсивные определения для всех ситуаций более последователен.
Что остается объяснить, так это результат этой головоломки.
Значение `s1` инициализируется `null`,
поэтому выражение `s1.length` заканчивается `NullPointerException` во время выполнения.
Инициализация значения `s2` фактически преобразуется в байтовый код,
эквивалентный `s2 = (new StringBuilder()).append(s2).append(s2).toString()`,
где аргумент `s2` имеет значение по умолчанию `null`.
Согласно JLS, раздел 15.18.1.1, ссылка `null` преобразуется в строку `"null"`,
а значение, которое присваивается `s2`, представляет собой строку `"nullnull"` длиной `8`.

## One Egg or Two..?

```dotty
class C
val x1, x2 = new C
val y1 @ y2 = new C
println(x1 == x2)
// false
println(y1 == y2)
// true
```

Согласно SLS (§4.1), определение значения `val p1, . . . , pn : T = e` —
это сокращение для последовательности определений значений `val p1 : T = e; ...; val pn : T = e`.
Это означает, что в нашем примере выражение `new C` вычисляется дважды, и поэтому `x1 == x2` ложно.
Второе определение значения — это связыватель шаблонов, определенный в SLS §8.1.3.
Связыватель шаблонов `x@p` состоит из переменной шаблона `x` и шаблона `p`.
В нашем случае шаблон `p` — это шаблон переменной, который соответствует любому значению
и связывает имя переменной (в нашем случае `y2`) с этим значением.
Связыватель шаблона связывает это значение также с переменной шаблона `y1`.
Поскольку к обеим переменным `y1` и `y2` привязано одно и то же значение, выражение `y1 == y2` истинно.

## Return to Me!

```dotty
def value: Int = 
  def one(x: Int): Int = { return x; 1 }
  val two = (x: Int) => { return x; 2 }
  1 + one(2) + two(3)

println(value)
// 3
```

Scala не жалуется на _unreachable code_, поэтому код компилируется нормально.
Если необходимо получать предупреждения о недостижимом коде, используйте параметр компилятора `-Ywarn-dead-code`.
Если необходимо видеть ошибку компилятора вместо предупреждения,
то дополнительно используйте опцию компилятора `-Xfatal-warnings`.
Ответ на этот вопрос можно найти в SLS §6.20:

> Выражение возврата return e должно встречаться внутри тела некоторого включающего его именованного метода или функции.
> Самый внутренний объемлющий именованный метод или функция в исходной программе, f,
> должен иметь явно объявленный тип результата, и тип e должен ему соответствовать.
> Выражение возврата оценивает выражение e и возвращает его значение как результат f.
> Оценка любых операторов или выражений, следующих за возвращаемым выражением, опускается.

Для первого оператора `return x` объемлющий именованный метод является методом номер один,
но для второго оператора `return x` объемлющий именованный метод является значением метода.
Когда функция `two(3)` вызывается как часть выражения `1 + one(2) + two(3)`,
то результат `3` возвращается как результат значения метода.
Кстати, возврат из вложенной анонимной функции реализуется путем создания
и перехвата `scala.runtime.NonLocalReturnControl`.
Наиболее распространенная причина, по которой вы действительно хотели бы вернуться из вложенной функции, —
это выход из императивного блока _for-comprehension_ или блока управления ресурсами.
См. ответы на вопрос о переполнении стека
[Цель оператора return в Scala?](https://stackoverflow.com/questions/3770989/purpose-of-return-statement-in-scala/3771243#3771243)
для дальнейшего обсуждения этого аспекта.

## Implicitly Surprising

```dotty
given z1: Int = 2

def addTo(n: Int) =
  def add(x: Int)(y: Int)(using z: Int) = x + y + z
  add(n) _

given z2: Int = 3
val addTo1 = addTo(1)
addTo1(2)
// val res0: Int = 5
addTo1(2)(3)
-- [E050] Type Error: ----------------------------------------------------------
1 |addTo1(2)(3)
|^^^^^^^^^
|method apply in trait Function1 does not take more parameters
|
| longer explanation available when compiling with `-explain`
1 error found
```

Когда eta расширение применяется к методу `add`, результатом является функция типа `Int => Int`,
т. е. неявные параметры разрешаются до применения eta расширения.
Поэтому неявное значение `z1 = 2` используется как значение неявного параметра `z`.
Если бы неявный параметр был недоступен, компилятор выдал бы сообщение об ошибке:

```dotty
scala> def add(x: Int)(y: Int)(using z: Int) = x + y + z
def add(x: Int)(y: Int)(using z: Int): Int

scala> add(1) _
-- Error: ----------------------------------------------------------------------
1 |add(1) _
  |      ^
  |     no given instance of type Int was found for parameter z of method add
1 error found
```

Можно также указать явное значение для `z`, если в текущем контексте не определено неявное значение,
но тогда тип должен быть указан в заполнителе.

```dotty
scala> val addTo1And3 = add(1)(_: Int)(using 3)
val addTo1And3: Int => Int = Lambda$9137/1728074700@1bd1b5e0
```

Это объясняет результат, т.е. вызов `addTo1(2)` возвращает `5 (1 + <param> + 2)`.
`addTo1(2)(3)` не компилируется, так как на объекте `5` (результат `addTo1(2)`) не определен метод `apply`.

```text
-- [E050] Type Error: ----------------------------------------------------------
1 |addTo1(2)(3)
  |^^^^^^^^^
  |method apply in trait Function1 does not take more parameters
  |
  | longer explanation available when compiling with `-explain`
1 error found
```

## One Bound, Two to Go

```dotty
def invert(v3: Int)(v2: Int = 2, v1: Int = 1): Unit =
  println(s"$v1, $v2, $v3")
  
def invert3 = invert(3) _

invert3(v1 = 2, v2 = 1)
// 1, 2, 3
```

```dotty
invert3(v1 = 2)
// error:
// missing argument for parameter v2 of method apply in trait Function2: (v1: Int, v2: Int): Unit
// invert3(v1 = 2)
// ^^^^^^^^^^^^^^^
```

Тип `invert3` после eta-расширения (SLS §6.26.5) больше не метод,
а функциональный объект, то есть экземпляр `Function2[Int, Int, Unit]`.
Об этом также сообщает REPL:

```text
scala> def invert3 = invert(3) _
def invert3: (Int, Int) => Unit
```

Этот функциональный объект имеет метод `apply` (унаследованный от `Function2[T1, T2, R]`)
со следующей сигнатурой: `def apply (v1: T1, v2: T2): R`
В частности, этот метод не определяет значений по умолчанию для своих аргументов!

Как следствие, `invert3(v1 = 2)` приводит к ошибке времени компиляции
(поскольку не хватает фактических аргументов для применения метода).
Имена аргументов `v1` и `v2` — это имена, определенные в методе применения `Function2[T1, T2, R]`.
Имена аргументов метода `apply` каждой функции с двумя аргументами имеют имена `v1` и `v2`,
в частности эти имена никак не связаны с именами аргументов метода `invert3`.
У метода `invert3` случайно есть аргументы с теми же именами, но, к сожалению, в другом порядке.
`invert3(v1 = 2, v2 = 1)` печатает` 1, 2, 3`,
так как параметр `v1` (соответствующий параметру `v2` в методе `invert`) имеет значение `2`,
а параметр `v2` (соответствующий параметру `v1` в методе `invert`) равен `1`.

## Count Me Now, Count Me Later

```dotty
var x = 0
def counter = 
  x += 1
  x
def add(a: Int)(b: Int) = a + b
val adder1 = add(counter)(_)
val adder2 = add(counter) _
println("x = " + x)
// x = 1
println(adder1(10))
// 12
println("x = " + x)
// x = 2
println(adder2(10))
// 11
println("x = " + x)
// x = 2
```

`f(a) _` и `f(a)(_)` имеют разные значения, регулируемые разными разделами спецификации языка Scala.

`f(a)(_)` — это синтаксис заполнителя для анонимных функций, описанный в SLS §6.23. Вычисление отложено.

`f(a) _` является eta-расширением, описанным в SLS §6.26.5.
Приведенные аргументы вычисляются по готовности; откладывается только сам вызов метода.

В этом примере eta-расширение в `val adder2 = add(counter) _`
вызывает немедленного вычисление, как описано:
как только эта строка запускается, Scala фактически вычисляет счетчик
и привязывает результат `1` к аргументу счетчика для `adder2`.
Следовательно, `x` равно `1` при первом выводе.
`adder1`, с другой стороны, является анонимной функцией,
поэтому ее аргумент-счетчик связан только при вычислении `adder1`.
Поскольку в этот момент `x` уже равен `1`, счетчик будет равен `2`, а вычисление `adder1` выведет `12`.
Значение `x` теперь равно `2`, как показано в выводе программы.

## What's in a Name?

```dotty
class C:
  def sum(x: Int = 1, y: Int = 2): Int = x + y
  
class D extends C:
  override def sum(y: Int = 3, x: Int = 4): Int = super.sum(x, y)

val d: D = new D
val c: C = d
c.sum(x = 0)
// res19: Int = 4
d.sum(x = 0)
// res20: Int = 3
```

Scala использует статический тип переменной для привязки имен параметров,
но значения по умолчанию определяются типом времени выполнения:

- Привязка имен параметров выполняется компилятором, и единственная информация,
  которую может использовать компилятор, — это статический тип переменной.
- Для параметров со значением по умолчанию компилятор создает методы,
  вычисляющие выражения аргументов по умолчанию (SLS §4.6).
  В приведенном выше примере оба класса `C` и `D` содержат методы `sum$default$1` и `sum$default$2`
  для двух параметров по умолчанию. Когда параметр отсутствует, компилятор использует результат этих методов,
  и эти методы вызываются во время выполнения.

## I Can Has Padding?

```dotty
import scala.StringBuilder

extension (sb: StringBuilder)
  def pad2(width: Int) =
    1 to width - sb.length foreach { sb append '*' }
    sb

// greeting.length == 14
val greeting = new StringBuilder("Hello, kitteh!")
// farewell.length == 9
val farewell = new StringBuilder("U go now.")  // I hate long bye-bye.
```

```dotty
println(greeting pad2 20)
// Hello, kitteh!*
```

```dotty
println(farewell pad2 20)
// java.lang.StringIndexOutOfBoundsException: index 10,length 10
```

Вспомните, что в Scala есть `StringBuilder`, который скрывает `java.lang.StringBuilder`, и у него есть метод `apply`.
`foreach` принимает функцию с параметром `Int`.
`StringBuilder.append` возвращает `StringBuilder`,
а `StringBuilder` расширяет `Function1`,
поэтому компилятор может просто использовать возвращенное значение в качестве аргумента для вызова `foreach`.
Каждая итерация вызывает `StringBuilder.apply`, который является псевдонимом для `StringBuilder.charAt`.
Код, сгенерированный компилятором, эквивалентен следующему:

```dotty
extension (sb: StringBuilder)
  def pad2(width: Int) =
    // the StringBuilder returned by append is a function!
    val sbAsFunction: Function1[Int, Char] = sb.append('*') // the same sb that was 
                                                            // passed to Padder
    (1 to (width - sb.length)).foreach(sbAsFunction)
    sb
```

Если развернуть цикл, причина исключения становится понятной.
В случае более короткой строки `StringBuilder` содержит только десять символов (`U go now.*` после вызова `append()`).
Таким образом, вызов `apply` - `charAt` - терпит неудачу на десятой итерации:

```dotty
def pad2(width: Int) =
  val sbAsFunction: Function1[Int, Char] = sb.append('*')
  sbAsFunction.apply(1)
  ...
  sbAsFunction.apply(9)
  sbAsFunction.apply(10) // fails here
  sbAsFunction.apply(11)
  sb
```

Можно получить ожидаемое поведение, явно указав литерал функции, который будет использоваться:

```dotty
import scala.StringBuilder

extension (sb: StringBuilder)
  def pad2(width: Int) =
    1 to (width - sb.length) foreach (_ => sb append '*')
    sb

// greeting.length == 14
val greeting = new StringBuilder("Hello, kitteh!")

// farewell.length == 9
val farewell = new StringBuilder("U go now.")  // I hate long bye-bye.
```

```dotty
println(greeting pad2 20)
// Hello, kitteh!******
println(farewell pad2 20)
// U go now.***********
```

## Cast Away

```dotty
import scala.jdk.CollectionConverters.*
def fromJava: java.util.Map[String, java.lang.Integer] = 
  val map = new java.util.HashMap[String, java.lang.Integer]()
  map.put("key", null)
  map

// watch out here...Integer is not Int!
val map = fromJava.asScala.asInstanceOf[scala.collection.Map[String, Int]]
println(map("key") == 0)
// true
```

```dotty
println(map("key") == null)
// error:
// Values of types Int and Null cannot be compared with == or !=
// println(map("key") == null)
//         ^^^^^^^^^^^^^^^^^^
```

Мотивация для этой головоломки связана с взаимодействием с библиотеками Java,
где может возникнуть соблазн «преобразовать» путем приведения типов в более «естественные» типы Scala.
В данном конкретном случае — от `java.lang.Integer` к `Int` —
типы, к сожалению, не совсем совпадают, и это, в конечном счете, источник головоломки.
Scala автоматически обрабатывает преобразования, такие как `java.lang.Integer` в `Int`, используя автоупаковку.
Если декомпилировать результирующий код с помощью декомпилятора Java, получим:

```
Predef$.MODULE$.println(BoxesRunTime.boxToBoolean(BoxesRunTime.unboxToInt($outer.map().apply("key")) == 0));
Predef$.MODULE$.println(BoxesRunTime.boxToBoolean($outer.map().apply("key") == null));
```

Интересной частью здесь является `BoxesRunTime.unboxToInt` в первом вызове,
который определяется как:

```
public static int unboxToInt(Object i) {
  return i == null ? 0 : ((java.lang.Integer)i).intValue();
}
```

Эта логика, которая обрабатывает `null` как `0`, отличается как Java Integer-to-int unboxing,
так и от scala.Int.unbox, которые оба вызывают исключение `NullPointerException` для `null`.

## Pick an Int, Any Int!

```dotty
class A:
  type X // equivalent to X <: Any
  var x: X = _
  
class B extends A:
  type X = Int
  
val b = new B
println(b.x)
// null
val bX = b.x
println(bX)
// 0
```

Поле `x` на уровне байт-кода является объектом (оно было объявлено в `A` и унаследовано).
`B` специализировал свой тип на `Int`, но используется то же хранилище.
Это означает, что поведение «неинициализированного `Int`» (в отличие от поведения «неинициализированной ссылки»)
зависит от содержимого поля, распаковываемого в `Int`.
Распаковка происходит, когда вызывается `b.x`, потому что `x` имеет тип `Int`.
Этого не происходит, когда выполняется `println(b.x)`,
потому что `println` принимает аргумент `Any`, поэтому ожидаемый тип выражения — `Any`.
Scala не понимает необходимости переводить представления, потому что выражение уже преобразовано в `Any`.

Сравнение:

```dotty
scala> println(b.x: Any)
null
scala> println(b.x: Int)
0
```

## A Case of Strings

```dotty
def objFromJava: Object = "string"
def stringFromJava: String = null

def printLengthIfString(a: AnyRef): Unit =
  a match
    case s: String => println("String of length " + s.length)
    case _ => println("Not a string")

printLengthIfString(objFromJava)
// String of length 6
printLengthIfString(stringFromJava)
// Not a string
```

Scala наследует от Java следующее поведение:

```dotty
val s: String = null  // допустимо
println(s.isInstanceOf[String]) // печатает "false", потому что, как и в Java, null.instanceof[String] == false
```

Затем это приводит к разрешению сопоставления с шаблоном.
Поэтому, если сопоставляется шаблон со значением, которое может быть `null`,
необходимо явно проверять значение `null`:

```dotty
a match
  case s: String => println("String of length " + s.length)
  case null      => println("Got null!")
  case _         => println("Something else...")
```

Разрешение сопоставления шаблонов основано на типе времени выполнения,
поэтому первый пример соответствует случаю `s: String`, даже если тип времени компиляции — `java.lang.Object`.
Хорошая идиома Scala состоит в том, чтобы преобразовать «может быть-нуль» из Java API в параметр:

```dotty
val str: Option[String] = Option(stringFromJava)
// str: Option[String] = None
str match
  case Some(s: String) => println("String of length " + s.length)
  case None => println("stringFromJava was null")
// stringFromJava was null
```

## A View to a Shill

```dotty
val ints = Map(1 -> List(1, 2, 3, 4, 5))
val bits = ints.map { case (k, v) => (k, v.iterator) }
val nits = ints.view.mapValues(_.iterator)
s"${bits(1).next}${bits(1).next}"
// res24: String = "12"
s"${nits(1).next}${nits(1).next}"
// res25: String = "11"
```

Как объясняется [в этом тикете](https://github.com/scala/bug/issues/4776) и в Скаладоке,
`mapValues` возвращает представление карты, которое сопоставляет каждый ключ этой карты с `f(this(key))`.
Полученная карта оборачивает исходную карту без копирования каких-либо элементов.
Каждое извлечение из обернутой карты приводит к новому вычислению функции отображения
и, в данном случае, к новому итератору.
Предписанное использование, когда требуется строгая коллекция: `(myMap mapValues (_.toIterator)).view.force`

## Accepts Any Args

```dotty
def acceptsAnyArgs(any1: Any)(any2: Any*): Unit =
  println(any1)
  println(any2)
acceptsAnyArgs("Psst", "hey", "world:")(4, 2)
// (Psst,hey,world:)
// ArraySeq(4, 2)
```

Возможно, это не широко известно и не разглашается, но Scala поддерживает "автоматическую корректировку" аргументов.
Чтобы соответствовать требованию первого аргумента типа `Any`, компилятор преобразует вызов в:

```text
... 
acceptsAnyArgs (( "Psst" , "hey" , "world:" ))( 4 , 2 )   
```

## The Devil is in the Defaults

```dotty
import collection.mutable

val goodies: Map[String, mutable.Queue[String]] =
  Map().withDefault(_ => mutable.Queue("No superheros here. Keep looking."))
val baddies: Map[String, mutable.Queue[String]] = Map().withDefaultValue(mutable.Queue("No monsters here. Lucky you."))

println(goodies("kitchen").dequeue)
// No superheros here. Keep looking.
println(baddies("in attic").dequeue)
// No monsters here. Lucky you.
println(goodies("dining room").dequeue)
// No superheros here. Keep looking.
println(baddies("under bed").dequeue)
// java.util.NoSuchElementException: empty collection
```

Увидев `withDefault`, может возникнуть соблазн подумать,
что `withDefaultValue` — это «фабрика», которая каким-то образом создает новый экземпляр
заданного значения по умолчанию при каждом вызове.
То, что на самом деле делает `withDefaultValue`, в значительной степени совпадает с тем,
что он говорит: он возвращает значение, которое ему было дано каждый раз.
Следовательно, если значение по умолчанию является изменяемым,
любые внесенные в него изменения повлияют на всех будущих вызывающих объектов.
Проще придерживаться `withDefault` всякий раз, когда требуется новое значение — изменяемое или неизменное,
и использовать `withDefaultValue` только тогда, когда каждый раз требуется один и тот же экземпляр.

## One, Two, Skip a Few

```dotty
val oneTwo = Seq(1, 2).permutations
// oneTwo: Iterator[Seq[Int]] = empty iterator
if oneTwo.length > 0 then
  println("Permutations of 1 and 2:")
  oneTwo foreach println
// Permutations of 1 and 2:

val threeFour = Seq(3, 4).permutations
// threeFour: Iterator[Seq[Int]] = empty iterator
if threeFour.nonEmpty then
  println("Permutations of 3 and 4:")
  threeFour foreach println
// Permutations of 3 and 4:
// List(3, 4)
// List(4, 3)
```


Тип результата метода `permutations`, даже когда он вызывается для `Seq`, как здесь, является `Iterator`.
Как объясняется в Scaladoc для `Iterator`:
«никогда не следует использовать итератор после вызова для него метода.
Два наиболее важных исключения также являются единственными абстрактными методами: `next` и `hasNext`».
В первом примере мы игнорируем это правило и пытаемся пройтись по элементам итератора после вызова метода `length`.
Вызов `length` фактически исчерпывает итератор, поэтому к тому времени,
когда мы попытаемся напечатать элементы, их не останется.
Во втором случае нам повезло, потому что `nonEmpty` реализован как `hasNext`.
Таким образом, не вызываются никакие другие методы, кроме разрешенных.
Затем можно успешно напечатать каждый из элементов, как и предполагалось.

## Oddly Enough

```dotty
var mkEven: Int => Int = _
def initMkEven(): Unit = 
  mkEven = (n: Int) => if n % 2 == 0 then n else return n + 1
initMkEven()

println(mkEven(2))
// 2
println(mkEven(3))
// scala.runtime.NonLocalReturnControl
```

Согласно SLS (§6.20), выражение `return` должно находиться
«внутри тела некоторого включающего именованного метода или функции»
и заставляет самый внутренний такой метод или функцию возвращать заданное значение.
Если выражение `return` встречается внутри анонимной функции, как в этом случае,
компилятор Scala не может преобразовать его в простую инструкцию возврата JVM.
Тело анонимной функции становится телом метода `apply`, сгенерированного компилятором,
и инструкция возврата JVM просто завершает этот метод применения.
Он не будет переходить к самому внутреннему вмещающему именованному методу или функции в программе, как планировалось.
Поэтому `return` из вложенной анонимной функции
вместо этого реализуется путем выбрасывания `scala.runtime.NonLocalReturnControl` в точке возвращаемого выражения
и перехвата его вокруг включающего метода или функции.
В этом случае рассматриваемый именованный метод называется `initMkEven`,
и компилятор вставляет вокруг него соответствующее предложение `catch`.
Однако оператор `return`, который фактически становится `throw new NonLocalReturnControl`,
выполняется после того, как мы вернулись из `initMkEven`.
Без «страховочной сетки», которая могла бы его поймать и обработать,
исключение во время выполнения возникает, когда мы вызываем `mkEven(3)`.

## Splitting Headache

```dotty
def validDna(maybeDna: String) = {
  def containsOnly(s: String, chars: Array[Char]) =
    s.split(chars).isEmpty

  containsOnly(maybeDna, Array('A', 'C', 'G', 'T'))
}

println(validDna("SAT"))
// false
println(validDna("AT"))
// true
println(validDna("A"))
// true
println(validDna(""))
// false
```

Метод `split(separators: Array[Char])` предоставляется `StringOps` и
разбивает строку вокруг вхождений заданного массива символов.
Таким образом, это может показаться хорошим кандидатом для проверки наличия других символов в строке:
если входная строка состоит только из символов-разделителей, между ними не должно быть подстрок,
и массив таких подстрок, возвращаемый разбиением, должен быть пустым.
Если присутствуют другие символы, они должны отображаться в массиве результатов.
Это работает, за исключением случая пустой строки.
В этом случае ни один из символов-разделителей не соответствует какой-либо части входной строки,
и, согласно Javadoc базового метода на `java.lang.String`,
результирующий массив содержит входную строку, т. е. пустую строку, в качестве своего единственного элемента.
Эта обработка пустой строки в результирующем массиве отличается от других примеров,
в которых входная строка состоит из ненулевого числа последовательных символов-разделителей.
В этих случаях подстроки между разделителями также пусты.
Однако эти пустые строки не появляются в возвращаемом массиве,
потому что базовый метод `split` для `java.lang.String` удаляет завершающие пустые строки из результата...
если во входной строке появляется хотя бы один разделитель.

## A Result, Finally!

```dotty
var errCount: Int = 0
def incErrs(): Int = { errCount += 1; errCount }

def tryEval(expr: => Double) =
  var res = Double.NaN
  try res = expr
  catch case _: Exception => incErrs()
  finally res

println(tryEval(10 / 4))
// ()
println(tryEval(10 / 0))
// 1
```

В Scala, если функция не завершается с помощью явного возвращаемого выражения,
значение, возвращаемое функцией, является значением последнего выражения в теле функции, которое должно быть выполнено.
Однако в этом отношении операторы в блоке `finally` не считаются «отдельными» выражениями в теле функции.
Последнее выражение в теле функции `tryEval` — это полное выражение `try...finally`, а не простое выражение `res`.
В соответствии с §6.22 Спецификации языка Scala значение, возвращаемое выражением `try...finally`,
является либо последним успешно вычисленным значением в блоке `try`,
либо, если возникает исключение, значением,
возвращаемым первым совпадающим случаем в блоке обработчик исключений.
В этом случае значения, возвращаемые из `tryEval`, равны `()` (поскольку присваивания в Scala не возвращают значение)
и `1` (результат увеличения счетчика ошибок в первый раз) соответственно.

## Heads You Win...

```dotty
import java.util.{List as JList, LinkedList}
import scala.jdk.CollectionConverters.*

def listFromJava: JList[Int] =
  val jlist = new java.util.LinkedList[Int]()
  jlist.add(1)
  jlist.add(2)
  jlist

def printHeadOrEmpty(s: collection.Seq[_]): Unit =
  s match
    case hd :: _ => println(hd)
    case _       => println("Empty :-(")

printHeadOrEmpty(listFromJava.asScala)
// Empty :-(
printHeadOrEmpty(listFromJava.asScala.toSeq)
// 1
```

Scala `CollectionConverters` превращает списки Java
в экземпляры `collection.mutable.Buffer`, поскольку списки Java изменяемы.
Сопоставление шаблона `hd :: _`, с другой стороны, является шаблоном конструктора,
использующим case класс `::`, который является неизменяемым списком.
Таким образом, первое совпадение не удается.
Однако второе совпадение завершается успешно, так как `toSeq` возвращает неизменяемую последовательность.
Чтобы сопоставить как неизменяемые, так и изменяемые последовательности, вместо этого используйте экстрактор `+:`.

## (Ex)Stream Surprise

```dotty
val nats: LazyList[Int] = 1 #:: (nats map { _ + 1 })
val odds: LazyList[Int] = 1 #:: (odds map { _ + 1 } filter { _ % 2 != 0 })
```

```dotty
nats filter { _ % 2 != 0 } take 2 foreach println
// 1
// 3
```

```dotty
odds take 2 foreach println
// java.lang.RuntimeException: self-referential LazyList or a derivation thereof has no more elements
```

Выражение `nats filter { _ % 2 != 0 }` создает новый ленивый список,
который извлекает элементы из `nats` и передает только те, которые соответствуют фильтру.
Таким образом, вторым элементом отфильтрованного ленивого списка является третий элемент `nats`, а именно значение `3`.
Последовательные элементы `nats` всегда можно вычислить,
потому что следующий элемент `nats` — это просто текущий элемент плюс `1`.
Однако в случае ленивого списка `odds` фильтр является частью рекурсивного определения ленивого списка.
Это означает, что единственными значениями, которые могут быть возвращены как элементы `odds`,
являются те, которые проходят фильтр.
Это не позволяет ленивому списку вычислять свой второй элемент,
то есть первый элемент `odds map { _ + 1 } filter { _ % 2 != 0 }`.
Первая попытка с использованием `1` (первый элемент коэффициента) не проходит фильтр.
Следующая попытка пытается использовать второй элемент `odds`,
но это именно то значение, которое мы пытаемся вычислить.
Таким образом, попытка вычислить второй элемент `odds` заканчивается рекурсивным вызовом самого себя,
что приводит к `self-referential LazyList or a derivation thereof has no more elements`.

## A Matter of Context

```dotty
def tmpDir(uniqueSuffix: String): String =
  """\\builder\tmp-""" + uniqueSuffix
def tmpDir2(uniqueSuffix: String): String =
  s"""\\builder\tmp-$uniqueSuffix"""
def tmpDir3(uniqueSuffix: String): String =
  s"\\builder\tmp-$uniqueSuffix"

println(tmpDir("42"))
// \\builder\tmp-42
println(tmpDir2("42"))
// \builder	mp-42
println(tmpDir3("42"))
// \builder	mp-42
```

При использовании строкового интерполятора `s` или любого другого метода интерполяции
обработка escape-последовательностей фактически не определяется «строковым выражением»,
следующим сразу за идентификатором.
Вместо этого, как [описано в документации](https://docs.scala-lang.org/overviews/core/string-interpolation.html),
строковое выражение, следующее за идентификатором, всегда обрабатывается как многострочный строковый литерал,
т.е. как если бы оно было заключено в тройные кавычки.
Выражения `s"""\\builder\tmp-${uniqueSuffix}"""` и `s"\\builder\tmp-${uniqueSuffix}"` переписываются как:

```dotty
StringContext("""\\builder\tmp-""").s(uniqueSuffix)
```

Как интерпретируются управляющие последовательности - зависит от реализации интерполятора.

В случае метода интерполяции `s` escape-последовательности интерпретируются так,
как если бы они были в строке с одинарными кавычками,
поэтому и второй, и третий операторы `println` печатают `\builder        mp-42`.

## Inference Hindrance

```dotty
case object Completed
given Conversion[Any, Completed.type] = _ => Completed
def log[T](t: T): T =
  println(s"Log: $t")
  t
def duplicate1(s: String): Completed.type =
  val res = log(s + s)
  res
def duplicate2(s: String): Completed.type =
  log(s + s)
duplicate1("Hello")
// Log: HelloHello
// res0: Completed = Completed
duplicate2("world")
// Log: Completed
// res1: Completed = Completed
```

В методе `duplicate1` тип значения `res` не указан и подразумевается как `String`.
Этот тип не соответствует ожидаемому возвращаемому типу метода `Completed.type`,
поэтому компилятор ищет применимое неявное преобразование (SLS §7.3).
Он находит `given Conversion[Any, Completed.type]`
и превращает последний оператор метода `duplicate1` в `(_ => Completed)(res)`.
Поэтому печатается `Log: HelloHello`.

В случае метода `duplicate2` компилятору сначала необходимо определить тип последнего оператора `log(s + s)`.
Поскольку возвращаемый тип журнала метода определяется его параметром типа,
то это означает, что компилятору необходимо выяснить значение параметра типа `T`
для этого конкретного вызова метода `log`.
Чтобы определить значение параметра типа, который не указан явно,
компилятор выполняет локальный вывод типа (SLS §6.26.4).
Эта процедура учитывает, среди прочего, ожидаемый тип оператора.
В этом случае ожидаемый тип — это возвращаемый тип метода `duplicate2`, т.е. `Completed.type`.
Определив, что значение параметра типа `T` для этого вызова метода `log` должно быть `Completed.type`,
компилятор затем должен выяснить, как поступить с аргументом `s + s`, переданным в `log`.
Это `String`, но, согласно сигнатуре типа `log`, он также должен быть `Completed.type`.
Компилятор снова ищет применимое неявное преобразование
и еще раз находит представление `given Conversion[Any, Completed.type]`.
Это представление вставляется вокруг аргумента, передаваемого в метод `log`,
поэтому последний оператор метода `duplicate2` становится `log[Completed.type]((_ => Completed)(s + s))`.
В результате вызов `log` из метода `duplicate2` выводит `Log: Completed`,
строковое представление объекта `Completed`.

## For Each Step...

```dotty
for
  x <- 1 to 2
  y <- { println("DEBUG 1: x: " + x); x to 1 }
do println(x + y)
// DEBUG 1: x: 1
// 2
// DEBUG 1: x: 2

for
  x <- 1 to 2
  _ = println("DEBUG 2: x: " + x)
  y <- x to 1
do println(x + y)
// DEBUG 2: x: 1
// DEBUG 2: x: 2
// 2
```

Несколько генераторов в `for comprehensions` и `for loops` приводят к вложенным вызовам `flatMap` (SLS §6.19):

```dotty
for {
    a <- expr1
    b <- expr2
    ...
}
``` 

преобразуются в код, эквивалентный:

```dotty
expr1 flatMap { a => expr2 flatMap ... }
``` 

Это означает, что операторы второго генератора – в первом примере `println("DEBUG 1: x: " + x); x to 1` —
выполняются всякий раз, когда первый генератор переходит к следующему элементу.
Это происходит только после того, как текущий элемент "полностью" прошел через выражение `for`.
В случае первого примера это означает, что оператор отладки, когда `x` имеет значение `2`,
печатается только после того, как оператор `println(x + y)` был выполнен для `x == 1`.

С другой стороны, определения значений фактически приводят к «преобразованию» генератора,
с которым они связаны (SLS §6.19):

```dotty
for {
    a <- expr1
    v = vexpr
    b <- expr2
    ...
}
```

преобразуются в код, эквивалентный:

```dotty
expr1 map { a => (a, vexpr) } flatMap { case (a, v) => expr2 flatMap ... }
```

Правостороннее выражение определения значения выполняется для каждого элемента первого генератора до того,
как элементы генератора будут переданы в оставшуюся часть выражения `for`.
Во втором примере это означает, что операторы отладки выполняются для каждого элемента выражения
от `1` до `2` перед первым выполнением (т.е. когда `x` имеет значение `1`) оператора `println(x + y)`.

## Beep Beep...Reversing

```dotty
import collection.SortedSet
val ints = SortedSet(-1, 1, 2)(summon[Ordering[Int]].reverse)
println(ints.filter(_ > 0).head)
// 2
println(ints.collect { case n if n > 0 => n }.head)
// 1
```

И `Set.filter`, и `Set.collect` сохраняют тип исходной коллекции и возвращают новые отсортированные наборы.
Однако, как и другие методы, которые могут возвращать элементы другого типа,
такие как `map` и `flatMap`, метод `collect` не сохраняет порядок исходной коллекции.
Есть перегруженные методы, принимающие `Ordering`, например, `Set.collect`:

```dotty
println(ints.collect { case n if n > 0 => n }(summon[Ordering[Int]].reverse).head)
// 2
```

или

```dotty
import collection.SortedSet
given Ordering[Int] = math.Ordering.Int.reverse
val ints = SortedSet(-1, 1, 2)
ints.collect { case n if n > 0 => n }.head
// res4: Int = 2
```

---

**Ссылки:**

- [Phillips Andrew, Šerifović Nermin - Scala Puzzlers][Scala Puzzlers]
  
[Scala Puzzlers]: https://scalapuzzlers.com/index.html 
