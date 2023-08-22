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

В этой главе кратко представлена полезная техника сравнения типов (показанная Jason Zaugg), 
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
Это не требует ничего особенного, и позже нам понадобятся Boolean-ы при сравнении чисел. 

Основные задействованные типы:

```dotty
sealed trait Bool
sealed trait True extends Bool
sealed trait False extends Bool
```

[Сравнение типов][match types] позволяет добавлять условие на заданный тип,
например:

```dotty
type Rep[A <: Bool] =
  A match
    case True  => Int
    case False => Long
    
summon[Rep[True] =:= Int]
// val res0: Int =:= Int = generalized constraint
summon[Rep[False] =:= Long]   
// val res1: Long =:= Long = generalized constraint 
```

Используя эту технику мы можем определить некоторые дополнительные типы в сопутствующем объекте `Bool`:

```dotty
object Bool:
  type &&[A <: Bool, B <: Bool] =
    A match
      case False => False
      case _     => B
  type ||[A <: Bool, B <: Bool] =
    A match
      case True => True
      case _    => B
  type Not[A <: Bool] =
    A match
      case True  => False
      case False => True
```

Пример использования:

```dotty
summon[((True && False) || Not[False]) =:= True]
// val res0: True =:= True = generalized constraint

// Аналогичный результат ниже
summon[(True && True) =:= True]
summon[(True && False) =:= False]
summon[(False && True) =:= False]
summon[(False && False) =:= False]

summon[(True || True) =:= True]
summon[(True || False) =:= True]
summon[(False || True) =:= True]
summon[(False || False) =:= False]

summon[Not[True] =:= False]
summon[Not[False] =:= True]
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
Bool.toBoolean[(True && False) || Not[False]]
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
type _3 = Succ[_2]
type _4 = Succ[_3]
type _5 = Succ[_4]
...
```
 
Сопоставление с шаблоном для типов позволяет нам деконструировать `Nat`. 
Например, мы можем объявить тип, который определяет, равен ли его параметр `_0`:

```dotty
type Is0[A <: Nat] =
  A match
    case _0 => True
    case _  => False
```

Для `_0` результатом деконструирования будет `True`, иначе - `False`.

```dotty
Bool.toBoolean[Is0[_0]] // true
Bool.toBoolean[Is0[_2]] // false
```

Исключительно для развлечения продолжим определение сравнений, сложения, умножения, модуля и возведения в степень.

## Часть 4b. Сравнение чисел Пеано

[Исходная статья][part 4b]

Далее давайте посмотрим на сравнение натуральных чисел. 
Определим тип сравнения следующим образом:

```dotty
sealed trait Comparison:
  type Match[IfLT <: Up, IfEQ <: Up, IfGT <: Up, Up] <: Up

  type gt = Match[False, False, True, Bool]
  type ge = Match[False, True, True, Bool]
  type eq = Match[False, True, False, Bool]
  type le = Match[True, True, False, Bool]
  type lt = Match[True, False, False, Bool]
```

Реализации выглядят так:

```dotty
sealed trait LT extends Comparison:
  type Match[IfLT <: Up, IfEQ <: Up, IfGT <: Up, Up] = IfLT
sealed trait EQ extends Comparison:
  type Match[IfLT <: Up, IfEQ <: Up, IfGT <: Up, Up] = IfEQ
sealed trait GT extends Comparison:
  type Match[IfLT <: Up, IfEQ <: Up, IfGT <: Up, Up] = IfGT
```

Затем мы можем определить тип `Compare`, сравнивающий два `Nat`. 

```dotty
type Compare[A <: Nat, B <: Nat] <: Comparison =
  A match
    case _0 =>
      B match
        case _0 => EQ
        case _  => LT
    case Succ[a] =>
      B match
        case _0      => GT
        case Succ[b] => Compare[a, b]
```

Для `A` равного `_0` результатом `Compare` является `EQ` если `B` равен `_0` (`A` и `B` равны), 
и `LT` (`A` меньше) - в противном случае.
Если же `A` - это `Succ[a]`, где `a` - переменная типа, предшествующего `A`,
то проверяем `B`. Если он равен `_0`, то результатом `Compare` является `GT` (`A` больше).
Если же он равен `Succ[b]`, то возвращаем результат сравнения предшествующих типов.

Получается потенциально бесконечная рекурсия на уровне типов.

Примеры:

```dotty
toBoolean[(_0 Compare _0)#eq]  // true
toBoolean[(_0 Compare _0)#lt]  // false
toBoolean[(_3 Compare _5)#lt]  // true
```

Когда мы доберемся до бинарного дерева поиска, 
`Compare` позволит использовать `Nat`-ы в качестве ключей в дереве. 
Далее мы определим общий способ рекурсии в `Nat` с использованием fold-ов.

## Часть 4c. Общая рекурсия по числам Пеано

[Исходная статья][part 4c]

К сожалению, определить свертывание на неопределенных типах невозможно,
поэтому арифметические операции будут реализовываться напрямую со сопоставлением типов с шаблоном.

## Часть 4d. Арифметика Пеано

[Исходная статья][part 4d]

Будем использовать [сравнение типов][match types] для определения арифметики натуральных чисел на уровне типов.

Функции уровня типа будут следовать форме функций уровня значения 
(в большинстве случаев мы просто применяем функцию `n` раз и ничего не делаем с текущим значением итерации).

Сложение выглядит так (что равносильно формуле: `a + b = (a - 1) + (b + 1)`):

```dotty
type Add[A <: Nat, B <: Nat] <: Nat =
  A match
    case _0      => B
    case Succ[a] => Add[a, Succ[B]]
```

Следующие проверки успешно компилируются:

```dotty
summon[Add[_0, _3] =:= _3]
summon[Add[_3, _0] =:= _3]
summon[Add[_3, _2] =:= _5]
```

Умножение определяется сложением (`a * b = a + a + ... + a`).
Вынесем случай `_0` в отдельный вариант, чтобы сэкономить время на вычисление.

```dotty
type Mult[A <: Nat, B <: Nat] <: Nat =
  A match
    case _0 => _0
    case _1 => B
    case Succ[a] =>
      B match
        case _0 => _0
        case _  => Add[Mult[a, B], B]
        
summon[Mult[_0, _3] =:= _0]
summon[Mult[_3, _0] =:= _0]
summon[Mult[_3, _2] =:= _6]        
```

Используя умножение можно определить факториал (`a! = a * (a - 1) * ... * 1`):

```dotty
type Fact[A <: Nat] <: Nat =
  A match
    case _0      => _1
    case Succ[a] => Mult[A, Fact[a]]
    
summon[Fact[_3] =:= _6]    
```

Возведение в степень с точки зрения умножения (\\(a^{b} = a * a * ... * a\\)):

```dotty
type Exp[A <: Nat, B <: Nat] <: Nat =
  B match
    case _0      => _1
    case Succ[b] => Mult[A, Exp[A, b]]
    
summon[Exp[_3, _0] =:= _1]
summon[Exp[_3, _1] =:= _3]
summon[Exp[_3, _2] =:= _9]    
```

Сравнение по модулю использует `Compare` и требует дополнительного объяснения.

Результат `A % B = ?`:

- в первую очередь запрещаем сравнивать по модулю `0`, добавляя ограничение `B <: Succ[?]`
- если `A` меньше `B`, то возвращаем `A`
- если они равны, то `0`
- если `A` больше `B`, то оно точно не равняется `0` и можно сразу вычленить "предыдущее число".
    - для вычисления модуля используем равенство: `A % B = (((A - 1) % B) + 1) % B`
    - рекурсия всегда заканчивается, потому что мы спускаемся от `A` до `B`, а затем возвращаем результат `(A - B) % B`
    - и так до тех пор, пока "разница" не станет меньше или равна `B`...

```dotty
type Mod[A <: Nat, B <: Succ[?]] <: Nat =
  A Compare B match
    case LT => A
    case EQ => _0
    case GT =>
      A match
        case Succ[a] => Mod[Add[Mod[a, B], _1], B]

summon[Mod[_5, _1] =:= _0]
summon[Mod[_5, _2] =:= _1]
summon[Mod[_5, _3] =:= _2]
summon[Mod[_5, _4] =:= _1]
summon[Mod[_5, _5] =:= _0]
summon[Mod[_5, _6] =:= _5]        
```

Для удобства определим тип сравнения:

```dotty
type Eq[A <: Nat, B <: Nat] <: Bool =
  Compare[A, B] match
    case EQ => True
    case _  => False
```

Несколько примеров:

```dotty
type Sq[N <: Nat] = Exp[N, _2]

toBoolean[Eq[Sq[_9], Add[_1, Mult[_8, _10]]]] // true
```

Операции с `Nat` легко реализовать с помощью сверток, 
но они плохо работают с достаточно большими числами или более сложными выражениями. 
Около `10_000` время компиляции сильно увеличивается или переполняется стек. 
Далее мы рассмотрим представление целых чисел без знака в двоичном виде и (вроде) решим проблему Эйлера № 4.


---

**Ссылки:**

- [Исходный код][source]
- ["Type-Level Programming in Scala"][blog]
    - [Part 1: Type Recursion][part 1]
    - [Part 2: implicitly and =:=][part 2]
    - [Part 3: Booleans][part 3]
    - [Part 4a: Peano number basics][part 4a]
        - [Peano numbers][wiki]
    - [Part 4b: Comparing Peano numbers][part 4b]
    - [Part 4c: General recursion on Peano numbers][part 4c]
    - [Part 4d: Peano arithmetic][part 4d]
- [Scala Reference - Match Types](http://dotty.epfl.ch/docs/reference/new-types/match-types.html#) 
- [In Dotty try match types as a replacement for type projections](https://stackoverflow.com/questions/59101681/dotty-cannot-infer-result-type-of-generic-scala-function-taking-type-parameter-t) 
- [Converting code using simple type projections to dotty](https://users.scala-lang.org/t/converting-code-using-simple-type-projections-to-dotty/6516)

[source]: https://gitflic.ru/project/artemkorsakov/scalabook/file?file=examples%2Fsrc%2Fmain%2Fscala%2Fru%2Fscalabook%2Fbooks%2Ftlpis&branch=master
[blog]: https://apocalisp.wordpress.com/2010/06/08/type-level-programming-in-scala/
[part 1]: https://apocalisp.wordpress.com/2010/06/08/type-level-programming-in-scala/
[part 2]: https://apocalisp.wordpress.com/2010/06/10/type-level-programming-in-scala-part-2-implicitly-and/
[part 3]: https://apocalisp.wordpress.com/2010/06/13/type-level-programming-in-scala-part-3-boolean/
[part 4a]: https://apocalisp.wordpress.com/2010/06/16/type-level-programming-in-scala-part-4a-peano-number-basics/
[part 4b]: https://apocalisp.wordpress.com/2010/06/17/type-level-programming-in-scala-part-4b-comparing-peano-numbers/
[part 4c]: https://apocalisp.wordpress.com/2010/06/21/type-level-programming-in-scala-part-4c-general-recursion-on-peano-numbers/
[part 4d]: https://apocalisp.wordpress.com/2010/06/21/type-level-programming-in-scala-part-4d-peano-arithmetic/
[wiki]: https://wiki.haskell.org/Peano_numbers
[match types]: ../scala/type-system/match-types.md
