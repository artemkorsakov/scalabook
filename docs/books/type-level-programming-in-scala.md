# Программирование на уровне типов в Scala

В этом разделе приведен перевод статей ["Type-Level Programming in Scala"][blog] с адаптацией под версию Scala 3.

## Часть 1: Рекурсия типов

[Исходная статья][part 1]

Система типов Scala допускает рекурсию, но не бесконечную рекурсию. 
Следующий код из примера выведет ошибку компиляции:

```dotty
// определение абстрактного типа и границ
trait Recurse:
  type Next <: Recurse
  // это определение рекурсивной функции
  type X[R <: Recurse] <: Int

// реализация
trait RecurseA extends Recurse:
  type Next = RecurseA
  // это реализация
  type X[R <: Recurse] = R#X[R#Next]

object Recurse:
  // ошибка компиляции: бесконечный цикл
  type C = RecurseA#X[RecurseA]
```

## Часть 2: implicitly и =:=

[Исходная статья][part 2]

В этом посте кратко представлена полезная техника сравнения типов (показанная Jason Zaugg), 
которая позже будет использоваться для проверки результатов вычислений на уровне типов.

Она использует метод `summon`, определенный в `Predef` как:

```dotty
transparent inline def summon[T](using x: T): x.type = x
```

Это полезно для перехвата неявного значения, которое находится в области видимости и имеет тип `T`. 
Неявное значение, которое нам нужно в этом случае, это `A =:= B` для некоторых типов `A` и `B`. 
`A =:= B` будет найдено только тогда, когда `A` того же типа, что и `B`.

Например, этот код компилируется:

```dotty
summon[Int =:= Int]
// val res0: Int =:= Int = generalized constraint
```

но следующий - нет:

```dotty
summon[Int =:= String]
// -- [E172] Type Error: ----------------------------------------------------------
// 1 |summon[Int =:= String]
//   |                      ^
//   |                      Cannot prove that Int =:= String.
// 1 error found
```

Также доступен `<:<` для соответствия типов.

Пример соответствия (`<:<`):

```dotty
summon[Int =:= AnyVal]
// -- [E172] Type Error: ----------------------------------------------------------
// 1 |summon[Int =:= AnyVal]
//   |                      ^
//   |                      Cannot prove that Int =:= AnyVal.
// 1 error found

summon[Int <:< AnyVal]
// val res0: Int =:= Int = generalized constraint
```

[Scala API](https://scala-lang.org/api/3.x/)

## Часть 3: Booleans

[Исходная статья][part 3]

Хорошим вводным примером программирования на уровне типов является [кодирование логических значений по Черчу](https://ru.wikipedia.org/wiki/%D0%9A%D0%BE%D0%B4%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5_%D0%A7%D1%91%D1%80%D1%87%D0%B0). 
Это не требует ничего особенного, и нам понадобятся Boolean-ы при сравнении чисел позже. 
Этот и следующий пост о числах Пеано на уровне типов предназначены для тех, кто не знаком с программированием на уровне типов.

Основные задействованные типы:

```dotty
sealed trait Bool
sealed trait True extends Bool
sealed trait False extends Bool
```

Мы можем добавить условные выражения, определив член типа `If` в `Bool`. 
`If` принимает три параметра: тип, который нужно создать, если `Bool` имеет значение `True`, 
тип, если он имеет значение `False`, и верхнюю границу, которой должны соответствовать первые два типа. 
Верхняя граница зачастую важна при работе с результатом `If`.

Наш расширенный `Bool` выглядит так:

```dotty
sealed trait Bool:
  type If[T <: Up, F <: Up, Up] <: Up
```

`True` и `False` просто возвращают соответствующий аргумент:

```dotty
sealed trait True extends Bool:
  type If[T <: Up, F <: Up, Up] = T
sealed trait False extends Bool:
  type If[T <: Up, F <: Up, Up] = F
```

Пример использования:

```dotty
summon[True#If[Int, Long, AnyVal] =:= Int]
// val res0: Int =:= Int = generalized constraint

summon[False#If[Int, Long, AnyVal] =:= Int]
// -- [E172] Type Error: ----------------------------------------------------------
// 1 |summon[False#If[Int, Long, AnyVal] =:= Int]
//   |                                           ^
//   |                    Cannot prove that False#If[Int, Long, AnyVal] =:= Int.
// 1 error found

summon[False#If[Int, Long, AnyVal] =:= Long]
// val res1: Long =:= Long = generalized constraint
```

Мы можем определить некоторые дополнительные типы в `Bool`:

```dotty
sealed trait Bool:
  type If[T <: Up, F <: Up, Up] <: Up
  type And[B <: Bool] = If[B, False, Bool]
  type Or[B <: Bool]  = If[True, B, Bool]
  type Not            = If[False, True, Bool]
```

Пример использования:

```dotty
summon[True#And[False]#Or[False#Not] =:= True]
// val res0: True =:= True = generalized constraint

// Аналогичный результат ниже
summon[True#And[True] =:= True]
summon[True#And[False] =:= False]
summon[False#And[True] =:= False]
summon[False#And[False] =:= False]

summon[True#Or[True] =:= True]
summon[True#Or[False] =:= True]
summon[False#Or[True] =:= True]
summon[False#Or[False] =:= False]

summon[True#Not =:= False]
summon[False#Not =:= True]
```

Мы также можем добавить в модуль `Bool` метод для преобразования типа `Bool` в логическое значение для прямой печати:

```dotty
object Bool:
  class BoolRep[B <: Bool](val value: Boolean)
  given BoolRep[False] = new BoolRep(false)
  given BoolRep[True]  = new BoolRep(true)

  def toBoolean[B <: Bool: BoolRep]: Boolean =
    summon[BoolRep[B]].value
```

Пример:

```dotty
Bool.toBoolean[True#And[False]#Or[False#Not]]
// val res0: Boolean = true
```

Это еще один метод проверки результата вычислений на уровне типов.

## Часть 4а. Основы числа Пеано

[Исходная статья][part 4a]

Как упоминалось во вступительной главе, мы можем выполнять рекурсию на уровне типов в Scala. 
Первым применением для этого будет представление чисел в системе типов ([числа Пеано][wiki]). 
Одним из их применений является безопасное для типов индексирование в `HLlists`, что мы сделаем в следующей главе. 
Основная идея, конечно, не нова, но мы все еще создаем инструменты, которые нам понадобятся для последующих глав.

Базовое представление неотрицательных целых чисел на уровне типа выглядит так:

```dotty
sealed trait Nat
sealed trait _0 extends Nat
sealed trait Succ[N <: Nat] extends Nat
```

Мы можем построить другие целые числа с помощью `Succ`:

```dotty
type _1 = Succ[_0]
type _2 = Succ[_1]
...
```

Как и в случае с `Bool`, мы добавляем некоторую структуру в `Nat`. 
Определим три основные операции, все на уровне типов.

Операция, необходимая для индексации в `HLists`, аналогична операции `If`, которую мы определили для `Bool`. 
Назовем её `Match`. 
Определение:

```dotty
sealed trait Nat:
  type Match[NonZero[N <: Nat] <: Up, IfZero <: Up, Up] <: Up
sealed trait _0 extends Nat:
  type Match[NonZero[N <: Nat] <: Up, IfZero <: Up, Up] = IfZero
sealed trait Succ[N <: Nat] extends Nat:
  type Match[NonZero[N <: Nat] <: Up, IfZero <: Up, Up] = NonZero[N]
```

Он похож на `Bool#If`, за исключением того, что мы принимаем конструктор типа для случая, когда `Nat` является `Succ`. 
То есть, если `Match` вызывается для `Succ[N]`, результатом будет `NonZero[N]`. 
Если фактический тип `Nat` равен `_0`, возвращается `IfZero`. 
Параметр `Up` определяет границу результата, как и для `Bool#If`.

Мы видим, что это позволяет нам деконструировать `Nat`. 
Например, мы можем объявить тип, который определяет, равен ли его параметр `_0`:

```dotty
sealed trait Nat:
  type Match[NonZero[N <: Nat] <: Up, IfZero <: Up, Up] <: Up
  private type ConstFalse[_] = False
  type Is0                   = Match[ConstFalse, True, Bool]
```

Для `_0` результатом `_0#Match` будет `True`. Для `Succ[N]` - `ConstFalse[N]`. 
`ConstFalse` всегда имеет значение `False`, поэтому для `Succ` результатом будет `False`.

```dotty
Bool.toBoolean[_0#Is0] // true
Bool.toBoolean[_1#Is0] // false
Bool.toBoolean[_2#Is0] // false
```

`Match` — единственная операция, которая нам нужна для `HList`. 
Мы продолжим определение сравнений, сложения, умножения, модуля и возведения в степень просто для развлечения.

## Часть 4b. Сравнение чисел Пеано

[Исходная статья][part 4b]




---

**Ссылки:**

- ["Type-Level Programming in Scala"][blog]
    - [Part 1: Type Recursion][part 1]
    - [Part 2: implicitly and =:=][part 2]
    - [Part 3: Booleans][part 3]
    - [Part 4a: Peano number basics][part 4a]
      - [Peano numbers][wiki]
    - [Part 4b: Comparing Peano numbers][part 4b]
    - [Part 4c: General recursion on Peano numbers][part 4c]
    - [Part 4d: Peano arithmetic][part 4d]


[blog]: https://apocalisp.wordpress.com/2010/06/08/type-level-programming-in-scala/
[part 1]: https://apocalisp.wordpress.com/2010/06/08/type-level-programming-in-scala/
[part 2]: https://apocalisp.wordpress.com/2010/06/10/type-level-programming-in-scala-part-2-implicitly-and/
[part 3]: https://apocalisp.wordpress.com/2010/06/13/type-level-programming-in-scala-part-3-boolean/
[part 4a]: https://apocalisp.wordpress.com/2010/06/16/type-level-programming-in-scala-part-4a-peano-number-basics/
[part 4b]: https://apocalisp.wordpress.com/2010/06/17/type-level-programming-in-scala-part-4b-comparing-peano-numbers/
[part 4c]: https://apocalisp.wordpress.com/2010/06/21/type-level-programming-in-scala-part-4c-general-recursion-on-peano-numbers/
[part 4d]: https://apocalisp.wordpress.com/2010/06/21/type-level-programming-in-scala-part-4d-peano-arithmetic/
[wiki]: https://wiki.haskell.org/Peano_numbers
