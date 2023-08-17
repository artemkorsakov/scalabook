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


---

**Ссылки:**

- ["Type-Level Programming in Scala"][blog]
    - [Part 1: Type Recursion][part 1]
    - [Part 2: implicitly and =:=][part 2]
    - [Part 3: Booleans][part 3]


[blog]: https://apocalisp.wordpress.com/2010/06/08/type-level-programming-in-scala/
[part 1]: https://apocalisp.wordpress.com/2010/06/08/type-level-programming-in-scala/
[part 2]: https://apocalisp.wordpress.com/2010/06/10/type-level-programming-in-scala-part-2-implicitly-and/
[part 3]: https://apocalisp.wordpress.com/2010/06/13/type-level-programming-in-scala-part-3-boolean/
