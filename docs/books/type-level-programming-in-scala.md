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

## Часть 4. Натуральные числа

## Часть 4а. Основы чисел Пеано

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

## Часть 4d. Арифметика чисел Пеано

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

## Часть 5. Двоичные числа уровня типа

## Часть 5a. Двоичные числа

[Исходная статья][part 5a]

Операции `Nat` обычно ограничиваются результатами ниже `10_000`. 
Время компиляции становится довольно продолжительным или переполняется стек. 
Альтернативой является использование двоичного представления чисел, названного здесь _Dense_ 
после реализации в ["Чисто функциональных структурах данных" Окасаки](https://books.google.ru/books?id=owgkEAAAQBAJ&printsec=frontcover&hl=ru#v=onepage&q&f=false). 
Мы будем использовать это представление для решения упрощенной задачи Project Euler в системе типов в следующей главе.

Здесь показаны только базовая форма и реализации приращения и свертки. 
См. полный исходный код `Add`, `Mult` и `Exp`.

Во-первых, нам нужен тип `Digit` для представления бита. 
Имеет два подтипа: Один (`One`) и Ноль (`Zero`). 
Мы определяем `Is0` для проверки на равенство `0` и `Compare` для сравнения цифр.

```dotty
sealed trait Digit
sealed trait Zero extends Digit
sealed trait One  extends Digit

type Is0[A <: Digit] =
  A match
    case Zero => True
    case One  => False

type Compare[A <: Digit, B <: Digit] <: Comparison =
  A match
    case Zero =>
      B match
        case Zero => EQ
        case One  => LT
    case One =>
      B match
        case Zero => GT
        case One  => EQ
```

Например:

```dotty
summon[Is0[Zero] =:= True]
summon[(One Compare Zero) =:= GT]
```

Далее мы создаем тип `Dense`. 
Это гетерогенный список уровня типа, в котором типы элементов ограничены цифрами. 
Начало списка является наименее значимым битом. 
Последний элемент непустого списка всегда равен единице и является старшим битом. 
Пустой список представляет ноль.

```dotty
sealed trait Dense:
  type digit <: Digit
  type tail <: Dense
  type ShiftR <: Dense
  type ShiftL <: Dense

sealed trait DNil extends Dense:
  type tail   = Nothing
  type digit  = Nothing
  type ShiftR = DNil
  type ShiftL = DNil

sealed trait DCons[d <: Digit, T <: Dense] extends Dense:
  type digit  = d
  type tail   = T
  type ShiftR = tail
  type ShiftL = Zero :: DCons[d, T]
```

Мы видим, что с `Dense` сдвиг происходит легче (по сравнению с `Nat`). 
Это просто конец списка (сдвиг вправо) или добавление нуля (сдвиг влево).

Мы можем заранее определить некоторые целые числа (используя псевдоним `::` для `DCons`):

```dotty
type ::[H <: Digit, T <: Dense] = DCons[H, T]

type _0 = DNil
type _1 = One :: DNil
type _2 = Zero :: One :: DNil
type _3 = One :: One :: DNil
type _4 = Zero :: Zero :: One :: DNil
type _5 = One :: Zero :: One :: DNil
...
```

Как обычно, определяем преобразование в значения:

```dotty
object Dense:
  def toInt[D <: Dense](using drep: DRep[D]): Int = drep.value

  final case class DRep[D <: Dense](value: Int)
  given DRep[DNil] = DRep[DNil](0)
  given dcons0ToRep[D <: Dense](using tailRep: DRep[D]): DRep[DCons[Zero, D]] =
    DRep(tailRep.value * 2)
  given dcons1ToRep[D <: Dense](using tailRep: DRep[D]): DRep[DCons[One, D]] =
    DRep(tailRep.value * 2 + 1)
```

Пример использования:

```dotty
toInt[_4] // 4
```

Следующее и предыдущее двоичное число определяются так:

```dotty
type Inc[T <: Dense] <: Dense =
  T match
    case DNil           => One :: DNil
    case DCons[Zero, t] => One :: t
    case DCons[One, t]  => Zero :: Inc[t]

type Dec[T <: Dense] <: Dense =
  T match
    case DNil             => Nothing
    case DCons[Zero, t]   => One :: Dec[t]
    case DCons[One, DNil] => DNil
    case DCons[One, t]    => Zero :: t
```

Пример:

```dotty
summon[Inc[_0] =:= _1]
summon[Inc[_1] =:= _2]
summon[Inc[_2] =:= _3]

summon[Dec[_0] =:= Nothing]
summon[Dec[_1] =:= _0]
summon[Dec[_2] =:= _1]
```

Операции сложения, умножения и возведения в степень определяются так:

```dotty
type Add[A <: Dense, B <: Dense] <: Dense =
  A match
    case DNil => B
    case DCons[Zero, ta] =>
      B match
        case DNil          => A
        case DCons[hb, tb] => hb :: Add[ta, tb]
    case DCons[One, ta] =>
      B match
        case DNil            => A
        case DCons[Zero, tb] => One :: Add[ta, tb]
        case DCons[One, tb]  => Zero :: Inc[Add[ta, tb]]
        
type Mult[A <: Dense, B <: Dense] <: Dense =
  A match
    case DNil             => DNil
    case DCons[One, DNil] => B
    case _ =>
      B match
        case DNil => DNil
        case _    => Add[Mult[Dec[A], B], B]   

type Exp[A <: Dense, B <: Dense] <: Dense =
  B match
    case DNil => _1
    case _    => Mult[A, Exp[A, Dec[B]]]

type Sq[N <: Dense] = Exp[N, _2]             
```

Пример:

```dotty
toInt[_5 Add _7]            // 12
toInt[_12 Mult _8 Mult _13] // 1248
toInt[_4 Exp _15]           // 1073741824
```

В некоторых случаях большие числа обрабатываются лучше, используя `Dense`, например, для степеней двойки.
В других же эффективнее `Nat` (например, для `_3#Exp[_8]`).

Позже `Dense` послужит хорошим примером ключей в ассоциативном массиве уровня типа. 
Далее мы решим (намного) уменьшенную версию задачи [Project Euler][Project Euler], используя двоичные числа уровня типа. 
Для этого нам понадобятся функции `Reverce` и `Compare`, определенные в `Dense`:

```dotty
type Compare[A <: Dense, B <: Dense] <: Comparison =
  A match
    case DNil =>
      B match
        case DNil => EQ
        case _    => LT
    case DCons[ha, ta] =>
      B match
        case DNil => GT
        case DCons[hb, tb] =>
          Compare[ta, tb] match
            case EQ => Digit.Compare[ha, hb]
            case LT => LT
            case GT => GT

type AddToTheEnd[D <: Digit, T <: Dense] <: Dense =
  T match
    case DNil        => D :: DNil
    case DCons[h, t] => h :: AddToTheEnd[D, t]

type Reverse[A <: Dense] <: Dense =
  A match
    case DNil        => DNil
    case DCons[h, t] => AddToTheEnd[h, Reverse[t]]            
```

Пример использования:

```dotty
summon[(_3 Compare _3) =:= EQ]
summon[(_3 Compare _5) =:= LT]
summon[(_5 Compare _3) =:= GT]

summon[Reverse[_4] =:= (One :: Zero :: Zero :: DNil)]
summon[Reverse[_8] =:= (One :: Zero :: Zero :: Zero :: DNil)]
summon[Reverse[_9] =:= _9]
```

## Часть 5b. Project Euler problem 4

[Исходная статья][part 5b]

Давайте посмотрим на решение [проблемы № 4 Project Euler][Project Euler, p4] на уровне типа. 
Поскольку мы работаем с двоичными числами в системе типов Scala, 
нам приходится существенно снижать наши ожидания относительно того, что мы можем решить.

Первое упрощение заключается в том, что мы будем рассматривать палиндромы по основанию `2`, 
поскольку наши числа уже находятся в этом представлении. 
Кроме того, мы не будем рассматривать большие числа. 
Мы просто найдем самый большой двоичный палиндром, который является произведением двух двоичных чисел меньше `N`. 
Даже `N = 15` требует времени, поэтому мы будем придерживаться `15`.

Я покажу подход, начав с обычного кода Scala, реализующего решение методом перебора:

```dotty
object E4:
  // класс, содержащий числа i,j и их произведение prod = i*j
  case class Result(prod: Int, iMax: Int, jMax: Int)

  // сравнивает Result-ы на основе произведения
  given Ordering[Result] with
    def compare(a: Result, b: Result): Int = a.prod compare b.prod

  // вычисляет наибольший бинарный палиндром,
  // являющийся произведением двух чисел, меньших start
  def apply(start: Int): Result =
    all(start).max

  // итерация по парам (i, j), вычисление произведения и отслеживание палиндромов
  def all(start: Int): Iterable[Result] =
    for
      i <- start to 1 by -1
      j <- i to 1 by -1
      prod = i * j
      if isPalindrome(prod)
    yield Result(prod, i, j)

  // проверка, что число является полиндромом в бинарном представлении
  def isPalindrome(i: Int): Boolean =
    i.toBinaryString == i.toBinaryString.reverse
```

Однако код нелегко перенести непосредственно на программирование на уровне типа. 
Нам нужно преобразовать его так, чтобы он явно рекурсировал, отслеживал и обновлял максимум, 
а не использовал промежуточные переменные. 
Мы также отказались от класса `Result` в пользу `Tuple3`.

```dotty
object E4_Explicit:
  opaque type Result = (Int, Int, Int)
  
  def apply(start: Int): Result = apply((1, 1, 1), start, start)

  def apply(max: Result, i: Int, j: Int): Result =
    apply1(max, (i * j, i, j))

  def apply1(max: Result, iteration: Result): Result =
    if isPalindrome(iteration._1) && iteration._1 > max._1 then
      apply2(iteration, iteration)
    else apply2(max, iteration)

  def apply2(max: Result, iteration: Result): Result =
    if iteration._3 == 1 then
      if iteration._2 == 1 then max
      else apply(max, iteration._2 - 1, iteration._2 - 1)
    else apply(max, iteration._2, iteration._3 - 1)

  def isPalindrome(i: Int): Boolean =
    i.toBinaryString == i.toBinaryString.reverse
```

Код все еще необходимо преобразовать в программирование на уровне типа. 
По сути, утверждения и сравнения "if" могут быть проблематичными. 
См. `Expanded` объект в [Euler4.scala](https://github.com/harrah/up/blob/master/Euler4.scala) 
для обычного кода Scala, наиболее близкого к следующему коду уровня типа.

Что касается кода уровня типа, давайте начнем с проверки бинарных палиндромов:

```dotty
type isPalindrome[D <: Dense] <: Bool =
  Reverse[D] Compare D match
    case EQ => True
    case _  => False
       
println(toBoolean[isPalindrome[_3]])
// true
println(toBoolean[isPalindrome[_6]])
// false
println(toBoolean[isPalindrome[_15]])
// true
```

Затем давайте проверим, является ли число палиндромом и больше текущего максимального палиндрома:

```dotty
type isGreaterThan[D <: Dense, Max <: Dense] <: Bool =
  D Compare Max match
    case GT => True
    case _  => False

type isLargerPalindrome[D <: Dense, Max <: Dense] =
  isPalindrome[D] && (D isGreaterThan Max)
 
println(toBoolean[isLargerPalindrome[_7, _3]])
// true
println(toBoolean[isLargerPalindrome[_3, _5]])
// false
println(toBoolean[isLargerPalindrome[_8, _5]])
// false
```

Перевод кода на разработку на уровне типов приводит к следующим результатам:

```dotty
trait Pali:
  type Result = (Dense, Dense, Dense)

  type Apply[
      max <: Dense,
      iMax <: Dense,
      jMax <: Dense,
      i <: Dense,
      j <: Dense,
      P <: Pali
  ]

trait Pali0 extends Pali:
  // Точка входа
  type App[start <: Dense, P <: Pali] =
    P match
      case _ => Apply[_1, _1, _1, start, start, P]

  // Рекурсия
  type Apply[
      max <: Dense,
      iMax <: Dense,
      jMax <: Dense,
      i <: Dense,
      j <: Dense,
      P <: Pali
  ] =
    P match
      case _ => Apply1[max, iMax, jMax, i, j, Mult[j, i], P]

  type Apply1[
      max <: Dense,
      iMax <: Dense,
      jMax <: Dense,
      i <: Dense,
      j <: Dense,
      prod <: Dense,
      P <: Pali
  ] =
    isLargerPalindrome[prod, max] match
      case True  => Apply2[prod, i, j, i, Dec[j], P]
      case False => Apply2[max, iMax, jMax, i, Dec[j], P]

  type Apply2[
      max <: Dense,
      iMax <: Dense,
      jMax <: Dense,
      i <: Dense,
      j <: Dense,
      P <: Pali
  ] =
    j match
      case _0 => Apply3[max, iMax, jMax, Dec[i], Dec[i], P]
      case _  => Apply3[max, iMax, jMax, i, j, P]

  type Apply3[
      max <: Dense,
      iMax <: Dense,
      jMax <: Dense,
      i <: Dense,
      j <: Dense,
      P <: Pali
  ] =
    i match
      case _0 => (max, iMax, jMax)
      case _  => Pali0#Apply[max, iMax, jMax, i, j, P]

end Pali0
```

Запуск дает следующие результаты:

```dotty
object Pali1 extends Pali0:
  summon[App[_7, Pali1.type] =:= (_21, _7, _3)]
  summon[App[_15, Pali1.type] =:= (Mult[_15, _13], _15, _13)]  
```

## Часть 6. Разнородные списки

## Часть 6a. Основы разнородных списков

[Исходная статья][part 6a]

`HList` — это кортеж произвольной длины.
Мы можем (в некоторой степени) легко перемещаться между длинами, объединяя кортежи или вытаскивая фрагмент из кортежа. 
Недостатки в том, что мы теряем специальный синтаксис для построения кортежей и типов, таких как `(Int, Boolean)` и `(3, true)`, 
иногда сталкиваемся с ограничениями компилятора или языка, а некоторые полезные операции требуют сложных типов.

`HList`-ы ранее были продемонстрированы [в Scala](http://jnordenberg.blogspot.com/2008/08/hlist-in-scala.html) 
(и даже [в Java](https://apocalisp.wordpress.com/2008/10/23/heterogeneous-lists-and-the-limits-of-the-java-type-system)). 
Реализация Scala использовала сочетание членов типа в иерархии `HList` и класса типов.

Здесь мы реализуем четыре основные операции, которые позволяют нам реализовать другие операции вне иерархии `HList` 
и без класса типов для каждой операции. 
Эти основные операции: добавление, свертка влево, свертка вправо и "заевшая молния" (stuck zipper). 
Под заевшей молнией подразумевается структура, которая указывает на позицию в `HList` 
и предоставляет `HList` до и после этой позиции, но не может перемещаться влево или вправо.

С помощью этих четырех операций мы можем определить:

- Использование сверток: длина, добавление, обращение, обратное добавление, последний элемент
- Использование "заевшей молнии": `at` (выбрать по индексу), `drop` (пропустить часть элементов), `take` (взять часть элементов),
  `replace` (заменить), `remove` (удалить), `map`/`flatMap` (преобразование по одному индексу), 
  `insert` (вставить элемент), `insert hlist` (вставить hlist), `splitAt` (разделить по заданному индексу)

С некоторыми дополнительными классами типов мы также определим:

- `zip` (поэлементное соединение двух списков)
- разнородный `apply` (применить `HList` функции к входящим `HList`)

Для начала создадим очень простой `HList`:

```dotty
sealed trait HList

case object HNil extends HList:
  self =>
  def ::[A](v: A): HCons[A, HNil.type] = HCons(v, self)

final case class HCons[H, T <: HList](head: H, tail: T) extends HList:
  self =>
  def ::[A](v: A): HCons[A, HCons[H, T]] = HCons(v, self)

// алиасы для построения типов HList и для сопоставления с шаблоном
object HList:
  type ::[H, T <: HList] = HCons[H, T]
  val :: = HCons
```

Это базовое определение уже полезно. Например, ниже показано типобезопасное построение и извлечение:

```dotty
// построение HList подобного Tuple3 ("str", true, 1.0)
val x = "str" :: true :: 1.0 :: HNil

// получение компонентов по вызову head/tail
val s: String  = x.head
val b: Boolean = x.tail.head
val d: Double  = x.tail.tail.head

// ошибка компиляции
// val e = x.tail.tail.tail.head

// или декомпозиция с помощью сопоставления с шаблоном
val f: (String :: Boolean :: Double :: HNil.type) => String =
  case "s" :: false :: _        => "test"
  case h :: true :: 1.0 :: HNil => h
  // ошибка компиляции из-за несовпадения конкретных типов и общей длины кортежа
  // case 3 :: "i" :: HNil => "invalid"
  case _ => "unknown"
```

Обратите внимание, что мы используем `::` для `HLists`, 
хотя на практике может быть лучше использовать `:+:` или другое имя, чтобы отличить его от `::`, 
используемого для объединения и сопоставления списков.

Далее мы определим свертку влево и вправо, 
которую можно использовать для простой реализации добавления, обращения, расчета длины и последнего элемента.

## Часть 6b. Свертка HList

[Исходная статья][part 6b]

Теперь, основываясь на базовом определении `HList`, реализуем свертки. 
Они будут отличаться от сверток, предложенных в исходной статье, 
потому что в Scala 3 нет возможности обращаться к неопределенным типам, как показано ниже:

```dotty
type Foldr[Value, F <: Fold[Any, Value], I <: Value] =
  F#Apply[H, tail.Foldr[Value, F, I]]
// Ошибка компилятора: F - это не конкретный тип, вызывать Apply на нем непозволительно  
```

### Fold

Определим трейт `Fold`, позволяющий сворачивать не только типы, но и значения:

```dotty
trait Fold[-Elem, Value]:
  type Apply[N <: Elem, Acc <: Value] <: Value
  def apply[N <: Elem, Acc <: Value](n: N, acc: Acc): Apply[N, Acc]
```

Тип `Apply` предназначен для сворачивания типов, а метод `apply` - для сворачивания значений.
`Value` — это тип результата и он же обеспечивает границу, позволяющую выполнять рекурсию на уровне типа.

Определим дополнительный трейт `FoldAny` для свертки разнородных списков (где `Elem` - это `Any`):

```dotty
trait FoldAny[Value] extends Fold[Any, Value]:
  type Foldr[H <: HList, I <: Value] <: Value =
    H match
      case HNil.type   => I
      case HCons[h, t] => Apply[h, Foldr[t, I]]

  def foldr[H <: HList, I <: Value](hlist: H, i: I): Value =
    hlist match
      case HNil              => i
      case HCons(head, tail) => apply(head, foldr(tail, i))

  type Foldl[H <: HList, I <: Value] <: Value =
    H match
      case HNil.type   => I
      case HCons[h, t] => Foldl[t, Apply[h, I]]

  def foldl[H <: HList, I <: Value](hlist: H, i: I): Value =
    hlist match
      case HNil              => i
      case HCons(head, tail) => foldl(tail, apply(head, i))
```

`I` — тип начального значения. 
Для пустого списка возвращаем его, а для непустого - разделяем список на головной элемент и остаточный список.
В зависимости от направления, в котором происходит свертка, либо сворачиваем головной элемент и результат рекурсии,
примененный к остатку, либо вызываем рекурсию для хвоста и свертки головного элемента и начального значения.

### Функции, использующие свертки

Мы можем определить функцию расчета длины списка на уровне типа в `HList`, используя `Inc`:

```dotty
type Inc = FoldAny[Nat] {
  type Apply[N <: Any, Acc <: Nat] = Succ[Acc]
}

type Length[H <: HList]            = Inc#Foldr[H, _0]
type LengthViaFoldLeft[H <: HList] = Inc#Foldl[H, _0]

// Проверка
summon[Length[x.type] =:= Nat._3]
summon[LengthViaFoldLeft[x.type] =:= Nat._3]
```

Используя функцию свертки, которая просто добавляет текущий тип итерации к накопленному типу `HList`, 
можно определить добавление, обращение и обращение с последующим добавлением на уровне типа:

```dotty
object AppHCons extends FoldAny[HList]:
  type Apply[N <: Any, H <: HList] = N :: H

  // будет использоваться позднее для реализации на уровне значений
  def apply[A, B <: HList](a: A, b: B) = HCons(a, b)

type :::[A <: HList, B <: HList] =
  AppHCons.Foldr[A, B]

type Reverse_:::[A <: HList, B <: HList] =
  AppHCons.Foldl[A, B]

type Reverse[A <: HList] =
  AppHCons.Foldl[A, HNil.type]
```

Определим методы расширения, позволяющие вычислять значения на уровне типов:

```dotty
object Length extends FoldAny[Int]:
  type Apply[N <: Any, Acc <: Int] = Int
  def apply[A, B <: Int](a: A, b: B): Apply[Any, Int] = b + 1

extension [A <: HList](a: A)
  def length: Int = Length.foldr(a, 0)

  def :::[B <: HList](b: B): HList = AppHCons.foldr(a, b)

  def reverse_:::[B <: HList](b: B): HList = AppHCons.foldl(a, b)

  def reverse: HList = AppHCons.foldl(a, HNil)
```

Некоторые примеры использования определенных выше методов:

```dotty
// построение разнородного списка длины 3
// со следующими типами Int :: String :: List[Char] :: HNil
val a = 3 :: "ai4" :: List('r', 'H') :: HNil

// построение разнородного списка длины 4
// со следующими типами Char :: Int :: Char :: String :: HNil
val b = '3' :: 2 :: 'j' :: "sdfh" :: HNil

// объединение двух HLists
val ab = a ::: b
// проверка значения путем сопоставления с шаблоном
val checkAB = ab match
  case 3 :: "ai4" :: List('r', 'H') :: '3' :: 2 :: 'j' :: "sdfh" :: HNil =>
    true
  case _ => false
require(checkAB)

// проверка длины HList
require(ab.length == 7)

// обратимость
val reversed = b.reverse
// проверка значения путем сопоставления с шаблоном
val checkReversed = reversed match
  case "sdfh" :: 'j' :: 2 :: '3' :: HNil => true
  case _                                 => false
require(checkReversed)

// обращение, а затем добавление
val reverseAppend = a reverse_::: b
// проверка значения
val checkReverseAppend = reverseAppend match
  case List('r', 'H') :: "ai4" :: 3 :: '3' :: 2 :: 'j' :: "sdfh" :: HNil =>
    true
  case _ => false
require(checkReverseAppend)
```

## Часть 6c. Индексирование HList

[Исходная статья][part 6c]

Далее мы реализуем некоторые операции индексации для `HList`-ов. 
Нам нужно отбросить или взять `n` элементов, получить элемент с индексом `i`, 
разделить `HList` на два списка `HList` по индексу `i` и т.д.

Идея состоит в том, чтобы взять `HList` и `Nat` и создать индексированную zip-структуру. 
Эта структура предоставляет значение и тип по индексу, заданному `Nat`. 
Также она предоставляет `HList` до и после индекса.

Однако `Indexed` — это застрявшая "молния". 
Он не может двигаться влево или вправо, а фиксируется на исходном индексе 
(к сожалению, даже эта застрявшая молния может стать проблемой для компилятора).

Базовый индексированный интерфейс выглядит так:

```dotty
sealed trait Indexed:
  type Before <: HList
  type At
  type After <: HList

  def withIndex[R](f: (Before, At, After) => R): R
```

Например, индексированный экземпляр для `Nat` равного `_2` и `HList` равного `3 :: true :: "blue" :: 'h' :: HNil`:

```dotty
new Indexed:
  type Before = Int :: Boolean :: HNil.type
  type At     = String
  type After  = Char :: HNil.type

  def withIndex[R](f: (Before, At, After) => R): R =
    f(3 :: true :: HNil, "blue", 'h' :: HNil)
```

Прежде чем рассмотреть создание экземпляра `Indexed` для произвольных `Nat` и `HList`, 
давайте убедимся, что можно реализовать несколько операций индексирования с помощью `withIndex`:

```dotty
// получение элемента по индексу
def at: At = withIndex((_, a, _) => a)

// удаление всех элементов перед заданным индексом
def drop: HCons[At, After] = withIndex((_, a, t) => HCons(a, t))

// получение всех элементов перед заданным индексом
def take: Before = withIndex((b, _, _) => b)

// замена значения текущего индекса на 'a'
def replace[A](a: A): HList = withIndex((b, _, t) => b ::: HCons(a, t))

// удаление значения текущего индекса
def remove: HList = withIndex((b, _, t) => b ::: t)

// применение функции 'f' к текущему значению
def map[B](f: At => B): HList = withIndex((b, a, t) => b ::: HCons(f(a), t))

// применение функции 'f', возвращающей HList, к текущему значению
// и вставка полученного списка на место текущего индекса
def flatMap[B <: HList](f: At => B): HList =
  withIndex((b, a, t) => b ::: f(a) ::: t)

// вставка заданного значения
def insert[C](c: C): HList =
  withIndex((b, a, t) => b ::: HCons(c, HCons(a, t)))

// вставка заданного списка HList в текущий индекс
def insertH[C <: HList](c: C): HList =
  withIndex((b, a, t) => b ::: c ::: HCons(a, t))

// возвращает список HList, лежащий перед текущим индексом, и HList, начинающийся с заданного индекса
def splitAt: (Before, ::[At, After]) =
  withIndex((b, a, t) => (b, HCons(a, t)))
```

Реализация разделена на два случая. 
Первый — `Indexed0`, представляющий местоположение указателя. 
Он указывает на головной элемент ячейки `HCons`. 
Хвост ячейки — это `After`, а `Before` пуст.

```dotty
final case class Indexed0[H, T <: HList](hc: H :: T) extends Indexed:
  type Before = HNil.type
  type At     = H
  type After  = T

  def withIndex[R](f: (Before, At, After) => R): R = f(HNil, hc.head, hc.tail)
```

`IndexedN` создает список `Before` `HList`. 
Он добавляет элемент в начало к другому `Indexed` `Before` 
и делегирует элементы `At` и `After` этому `Indexed`. 
В конечном счете, завершающий `Indexed` должен быть `Indexed0`.

```dotty
final case class IndexedN[H, I <: Indexed](h: H, iTail: I) extends Indexed:
  type Before = H :: iTail.type#Before
  type At     = iTail.type#At
  type After  = iTail.type#After

  def withIndex[R](f: (Before, At, After) => R): R =
    iTail.withIndex((before, at, after) => f(HCons(h, before), at, after))
```

Теперь нам нужно получить индексированный объект для `HList`. 
Для этого мы определяем тип `toI[H <: HList, N <: Nat]`. 
Этот элемент типа определяет индексированный тип для `HList` и `Nat`. 
Затем мы можем использовать неявные выражения для предоставления этого типа.

```dotty
type toI[H <: HList, N <: Nat] <: Indexed =
  H match
    case HNil.type   => Nothing
    case HCons[h, t] =>
      // совпадение по N:
      //   Если он равен _0, тип Indexed должен 
      //     указывать на эту ячейку (то есть это Indexed0)
      //   в противном случае индекс будет справа, 
      //     поэтому вернитесь к N-1 и верните IndexedN
      N match
        case _0      => Indexed0[h, t]
        case Succ[n] => IndexedN[h, toI[t, n]] 
```

Итак, учитывая `HList h` и индекс `Nat N`, мы знаем, что нам нужен индексированный тип `toI[A, N]`. 
Нам нужна функция `A => toI[A, N]`. 
Мы можем сделать это с помощью неявных значений следующим образом:

```dotty
// определено как метод расширения для HList
extension [A <: HList](a: A)
  ...
  def i[N <: Nat](using in: A => toI[A, N]): toI[A, N] = in(a)

object Indexed:
  given indexed0[H, T <: HList]: (HCons[H, T] => Indexed0[H, T]) =
    hc => new Indexed0[H, T](hc)

  given indexedN[H, T <: HList, I <: Indexed](using
      iTail: T => I
  ): (HCons[H, T] => IndexedN[H, I]) =
    hc => new IndexedN[H, I](hc.head, iTail(hc.tail))
```

Примеры:

```dotty
val x = 3 :: true :: "asfd" :: false :: 'k' :: () :: 13 :: 9.3 ::  HNil
 
// получение булева значения по индексу 3
val b2: Boolean = x.i[_3].at
// false
 
// удалить все значения перед индексом 3 и затем получить первое значение
val pre: Boolean = x.i[_3].drop.i[_0].at
// false
 
// замена булева значения по индексу 3 на число 19
val rep = x.i[_3].replace(19)
// 3 :: true :: "asfd" :: 19 :: 'k' :: () :: 13 :: 9.3 :: HNil

// преобразование значения по индексу 4 на `true` если символ в нижнем регистре, иначе - `false`
val mp = x.i[_4].map(_.isLower)
// 3 :: true :: "asfd" :: false :: true :: () :: 13 :: 9.3 :: HNil
 
// Удаление значения по индексу 5
val rm = x.i[_5].remove
// 3 :: true :: "asfd" :: false :: 'k' :: 13 :: 9.3 :: HNil
 
// преобразование значения по индексу 2 на HList, созданного на основе значения
val fmp = x.i[_2].flatMap( s => s.charAt(0) :: s.substring(1) :: HNil )
// 3 :: true :: 'a' :: "sfd" :: false :: 'k' :: () :: 13 :: 9.3 :: HNil

// вставка значения в начало HList
val ins0 = x.i[_0].insert(List(3,4))
// List(3, 4) :: 3 :: true :: "asfd" :: false :: 'k' :: () :: 13 :: 9.3 :: HNil 
 
// вставка значения по индексу 7
val ins7 = x.i[_7].insert(-3.0f)
// 3 :: true :: "asfd" :: false :: 'k' :: () :: 13 :: -3.0 :: 9.3 :: HNil
    
// вставка HList по индексу 3
val insH = rm.i[_3].insertH( 'h' :: b2 :: Some(3) :: None :: HNil )
// 3 :: true :: "asfd" :: 'h' :: false :: Some(3) :: None :: false :: 'k' :: 13 :: 9.3 :: HNil 

// разбиение HList по индексу 6
val (aa, bb) = ins7.i[_6].splitAt
// (3 :: true :: "asfd" :: false :: 'k' :: () :: HNil, 13 :: -3.0 :: 9.3 :: HNil)
 
// аналог удаления справа
val dropRight = x.reverse.i[_3].drop.reverse
// 3 :: true :: "asfd" :: false :: 'k' :: HNil 
```

Далее мы увидим, как определить `zip` и `unzip` для объединения и разделения HL-списков кортежей.

## Часть 6d. HList Zip/Unzip

[Исходная статья][part 6d]

Для `zip` и `unzip` нужно определить некоторые классы типов. 
Сначала мы определим класс типов `HZip`, который принимает два списка `HList` и создает сжатый список `HList`.

```dotty
sealed trait HZip[A <: HList, B <: HList, Result <: HList]:
  def apply(a: A, b: B): Result
```

Реализуем класс типов с двумя основными вариантами: 

- `HZip[HNil.type, HNil.type, HNil.type]`
- `HZip[HA :: TA, HB :: TB, (HA, HB) :: TR]`.

Сжатие двух `HNil` создает `HNil`:

```dotty
given HZip[HNil.type, HNil.type, HNil.type] with
  def apply(a: HNil.type, b: HNil.type): HNil.type = HNil
```

Сжатие двух ячеек `HCons` объединяет их головные элементы в кортеж, сжимает их хвосты 
и создает новую ячейку `HCons` из кортежа и сжатых хвостов.

```dotty
given hzipCons[HA, HB, TA <: HList, TB <: HList, TR <: HList](using
    hzipTail: HZip[TA, TB, TR]
): HZip[HA :: TA, HB :: TB, (HA, HB) :: TR] with
  def apply(a: HA :: TA, b: HB :: TB): (HA, HB) :: TR =
    HCons((a.head, b.head), hzipTail(a.tail, b.tail))
```

Два дополнительных случая, `hzipNil1` и `hzipNil2`, 
обрабатывают несовпадающие длины путем простого усечения более длинного списка.

```dotty
given hzipNil1[H, T <: HList]: HZip[H :: T, HNil.type, HNil.type] with
  def apply(a: H :: T, b: HNil.type): HNil.type = HNil

given hzipNil2[H, T <: HList]: HZip[HNil.type, H :: T, HNil.type] with
  def apply(a: HNil.type, b: H :: T): HNil.type = HNil
```

Мы подключаем эти реализации к методам расширения и можем вызывать `zip` непосредственно в `HList`.

```dotty
extension [A <: HList](a: A)
  def zip[B <: HList, R <: HList](b: B)(using hzip: HZip[A, B, R]): R =
    hzip(a, b)
```

Примеры:

```dotty
// разнородный список длины 3
// с типом Int :: String :: List[Char] :: HNil
val a = 3 :: "ai4" :: List('r', 'H') :: HNil

// разнородный список длины 4
// с типом Char :: Int :: Char :: String :: HNil
val b = '3' :: 2 :: 'j' :: "sdfh" :: HNil

// два свернутых HLists
val c = a zip b
// (3, '3') :: ("ai4", 2) :: (List('r', 'H'), 'j') :: HNil

// ещё одна свертка
val cc = c zip c.tail
// ((3, '3'), ("ai4", 2)) :: (("ai4", 2), (List('r', 'H'), 'j')) :: HNil

// проверка типов
// заметим, что четвертый элемент b удален,
// также как это происходит при свертке однородных списков List
val checkCType: (Int, Char) :: (String, Int) :: (List[Char], Char) :: HNil.type = c

val checkCCType: ((Int, Char), (String, Int)) :: ((String, Int), (List[Char], Char)) :: HNil.type = cc
```

Далее мы определим класс типов `unzip`, который принимает `HList` кортежей и разделяет его на два `HList` по компонентам:

```dotty
trait Unzip[H <: HList, R1 <: HList, R2 <: HList]:
  def unzip(h: H): (R1, R2)
```

При распаковке `HNil` создается `HNil`.

```dotty
object Unzip:
  given Unzip[HNil.type, HNil.type, HNil.type] with
    def unzip(h: HNil.type) = (HNil, HNil)
```

Для `HCon` мы разархивируем остаток, отделяем компоненты заголовка
и добавляем соответствующий компонент заголовка к каждому компоненту остатка.

```dotty
given unzipCons[H1, H2, T <: HList, TR1 <: HList, TR2 <: HList](using
    unzipTail: Unzip[T, TR1, TR2]
): Unzip[(H1, H2) :: T, H1 :: TR1, H2 :: TR2] with
  def unzip(h: (H1, H2) :: T): (H1 :: TR1, H2 :: TR2) =
    val (t1, t2) = unzipTail.unzip(h.tail)
    (HCons(h.head._1, t1), HCons(h.head._2, t2))
```

Опять же, нам просто нужно подключить добавить метод расширения к `HList`.

```dotty
extension [A <: HList](a: A)
  def unzip[R1 <: HList, R2 <: HList](using un: Unzip[A, R1, R2]): (R1, R2) =
    un unzip a
```

Основываясь на примере выше,

```dotty
// разархивирование свернутых HList-ов
val (cc1, cc2) = cc.unzip

val (ca, cb) = cc1.unzip
// ca = 3 :: "ai4" :: HNil
// cb = '3' :: 2 :: HNil

// проверка типов
val checkCC1: (Int, Char) :: (String, Int) :: HNil.type        = cc1
val checkCC2: (String, Int) :: (List[Char], Char) :: HNil.type = cc2
val checkCa: Int :: String :: HNil.type                        = ca
val checkCb: Char :: Int :: HNil.type                          = cb
```

Далее мы рассмотрим применение функций к значениям `HList`.

## Часть 6e. HList Apply

[Исходная статья][part 6e]

Мы продолжим определение `happly`, разнородного метода применения функций. 
Он принимает `HList` функций и применяет их к соответствующим значениям во входном `HList`, 
создавая `HList` результатов.

Во-первых, пример использования выглядит так:

```dotty
// два HList с типом Double :: Char :: HNil
val y1 = 9.75 :: 'x' :: HNil
val y2 = -2.125 :: 'X' :: HNil

// Функции применения.
// Тип z: (Double => Double) :: (Char => Boolean) :: HNil
val z = ((_: Double) + .5) :: ((_: Char).isUpper) :: HNil

// применение первого параметра HList z ко второму параметру y1
val z1 = happly(z)(y1)
// проверка типов
val z1Types: Double :: Boolean :: HNil.type = z1
// проверка значения
assertEquals(z1, 10.25 :: false :: HNil)

// применение первого параметра HList z ко второму параметру y2
val z2 = happly(z)(y2)
// проверка типов
val z2Types: Double :: Boolean :: HNil.type = z2
// проверка значения
assertEquals(z2, -1.625 :: true :: HNil)
```

Мы реализуем `Happly`, используя класс типов `HApply`, который по сути является `Function1`. 
На самом деле мы не можем использовать `Function1`, 
потому что мешают существующие неявные условия, связанные с `Function1`.

```dotty
sealed trait HApply[-In, +Out]:
  def apply(in: In): Out
```

Идея состоит в том, что, учитывая `HList` функций, мы создаем `HApply`, 
который принимает `HList` параметров "правильного" типа и выдает `HList` результатов. 
Для функции `z` из примера нам нужен `HApply` типа:

```dotty
HApply[(Double :: Char :: HNil), (Double :: Boolean :: HNil)]
```

Для этого есть два основных случая: `HNil` и `HCons`. 
Самый простой случай — это сопоставление `HNil` с `HNil`:

```dotty
given HApply[HNil.type, HApply[HNil.type, HNil.type]] with
  def apply(h: HNil.type): HApply[HNil.type, HNil.type] =
    new HApply[HNil.type, HNil.type]:
      def apply(in: HNil.type): HNil.type = HNil
```

Как обычно, интересен случай с `HCons`. 
Мы принимаем ячейку `HCons` с заголовком, являющимся функцией, 
и создаем `HApply`, который будет принимать входную ячейку `HCons` требуемого типа. 
Затем `HApply` применяет функцию `head` к значению `head` и выполняет рекурсию по остатку. 
`HApply`, используемый для рекурсии, предоставляется неявно, и именно поэтому мы требуем, 
чтобы один `HList` был `HList`, полностью состоящим из функций, 
а другой `HList` содержал значения правильного типа, 
которые будут предоставляться в качестве входных данных для этих функций.

```dotty
given happlyCons[InH, OutH, TF <: HList, TIn <: HList, TOut <: HList](using
    applyTail: HApply[TF, HApply[TIn, TOut]]
): HApply[(InH => OutH) :: TF, HApply[InH :: TIn, OutH :: TOut]] with
  def apply(h: (InH => OutH) :: TF): HApply[InH :: TIn, OutH :: TOut] =
    new HApply[InH :: TIn, OutH :: TOut]:
      def apply(in: InH :: TIn): OutH :: TOut =
        HCons(h.head(in.head), applyTail(h.tail)(in.tail))
```

В примере мы имеем:

```dotty
val y1 = 9.75 :: 'x' :: HNil

val z = ((_: Double) + .5) :: ((_: Char).isUpper) :: HNil
```

Итак, для `happly(z)(y1)` наша неявная конструкция строится с помощью:

```dotty
happlyCons[Double, Double, Char => Boolean :: HNil, Char :: HNil, Boolean :: HNil]( 
  happlyCons[Char, Boolean, HNil, HNil, HNil](
    happlyNil
  )
)
```

Первый метод `applyCons` создает `HApply`, который использует заголовок `z`, 
функцию типа `Double => Double`, для сопоставления `HList` с заголовком типа `Double` с `HList` с заголовком типа `Double`. 
Он использует `HApply` из второго `happlyCons` для отображения остатка входного `HList`.

Этот второй `HApply` использует второй элемент `z`, функцию типа `Char => Boolean`, 
для сопоставления `HList` с заголовком типа `Char` с `HList` с заголовком типа `Boolean`. 
Поскольку это последний элемент, рекурсия заканчивается отображением `happlyNil` `HNil` в `HNil`.

Наконец, мы определяем точку входа. 
Учитывая `HList` функций и `HList` аргументов этих функций, мы используем `happly`, 
чтобы неявно получить `HApply` и создать с его помощью результирующий `HList`:

```dotty
def happly[H <: HList, In <: HList, Out <: HList](h: H)(in: In)(using
    toApply: HApply[H, HApply[In, Out]]
): Out =
  toApply(h)(in)
```

## Часть 6f. Получение экземпляров классов типов с помощью HLists

[Исходная статья][part 6f]

Хотя некоторые части этой серии не имеют прямого практического применения 
([Project Euler problem 4 в системе типов][part 5b]), сами `HLists` определенно практичны. 
Механизм задач в sbt позволяет избежать необходимости использования большого количества шаблонов 
или использования препроцессора, сохраняя при этом безопасность типов за счет использования `HList`-ов 
(и `KList`-ов — гетерогенного списка с конструктором типа, общим для каждой ячейки, который будет обсуждаться в части 8).

Однако в этой главе давайте посмотрим, как мы можем использовать `HList`-ы, чтобы уменьшить еще одну категорию шаблонов. 
Это связано с определением экземпляров классов типов, особенно тех, которые созданы для определенного типа данных. 
В примерах используется `scala.Equiv`. 
Если вы не знакомы с `Equiv`, это класс типов для равенства:

```dotty
trait Equiv[T]:
   def equiv(a: T, b: T): Boolean
```

Экземпляр для `Int` может выглядеть так:

```dotty
given Equiv[Int] with
  def equiv(a: Int, b: Int): Boolean = a == b
```

Для `Tuple2` мы создаем экземпляры `Equiv` для его членов:

```dotty
given pairEquiv[A, B](using a: Equiv[A], b: Equiv[B]): Equiv[(A, B)] with
  def equiv(t1: (A, B), t2: (A, B)): Boolean =
    a.equiv(t1._1, t2._1) && b.equiv(t1._2, t2._2)
```

Нам нужно будет повторить это для каждого `TupleN`. Это немного раздражает, но не так уж плохо. 
Однако классы пользователей представляют собой препятствие. 
В частности, мы будем рассматривать кейс-классы, поскольку Scala фокусируется на создании методов для них. 
Очевидно, что пользователи могут создавать столько кейс-классов, сколько захотят. 
Вы не можете заранее написать `Equiv` для всех этих классов. 
Вместо этого для каждого кейс-класса пользователю необходимо сделать что-то вроде:

```dotty
case class Example[A, B](a: A, b: Seq[B], c: Int)

given exampleEquiv[A, B](using
    a: Equiv[A],
    b: Equiv[Seq[B]],
    c: Equiv[Int]
): Equiv[Example[A, B]] with
  def equiv(e1: Example[A, B], e2: Example[A, B]): Boolean =
    a.equiv(e1.a, e2.a) && b.equiv(e1.b, e2.b) && c.equiv(e1.c, e2.c)
```

Это чисто шаблонный подход, поскольку мы не говорим ничего нового. Мы дублируем логику `Equiv` для `Tuple3`. 
Класс `Example` по сути представляет собой `Tuple3[A, Seq[B], Int]`, 
а `HList`-ы способны представлять кортежи произвольной длины. 
Мы могли бы написать функции для преобразования между `HList`-ами и нашими кейс-классами. 
Если мы затем создадим экземпляры нашего класса типов для `HCons` и `HNil`, 
а также для любого кейс-класса, который обеспечивает преобразование в `HList`-ы и обратно, 
то сможем автоматически получить экземпляры класса типа для любого кейс-класса.

Сопутствующий объект кейс-класса содержит неявные преобразования в соответствующий тип `HList` и обратно. 
Для приведенного выше класса `Example` это примерно эквивалентно следующему определению, данному вручную:

```dotty
object Example:
  extension [A, B](e: Example[A, B])
    def toHList: A :: Seq[B] :: Int :: HNil.type = e.a :: e.b :: e.c :: HNil

  def fromHList[A, B](hl: A :: Seq[B] :: Int :: HNil.type): Example[A, B] =
    val a :: b :: c :: HNil = hl
    Example(a, b, c)
```

Затем мы реализуем `Equiv` для `HList` и для всего, что конвертируется в `HList`:

```dotty
object EquivHList:
  // HNil === HNil
  given Equiv[HNil.type] with
    def equiv(a: HNil.type, b: HNil.type) = true

  // заданы Equiv для заголовка и остатка,
  //   (h1 :: t1) === (h2 :: t2) тогда, когда
  //   (h1 === h2) и (t1 === t2)
  given equivHCons[H, T <: HList](using
      equivH: Equiv[H],
      equivT: Equiv[T]
  ): Equiv[HCons[H, T]] with
    def equiv(a: HCons[H, T], b: HCons[H, T]): Boolean =
      equivH.equiv(a.head, b.head) && equivT.equiv(a.tail, b.tail)

  // дано:
  //   - тип T, конвертируемый в HList типа HL
  //   - экземпляр Equiv для HL
  // мы можем создать Equiv[T] приведя T к HL и используя Equiv[HL]
  given equivHListable[T, HL <: HList](using
      toHList: T => HL,
      equivHL: Equiv[HL]
  ): Equiv[T] with
    def equiv(a: T, b: T): Boolean =
      equivHL.equiv(toHList(a), toHList(b))
```

Итак, нам нужно написать что-то вроде `EquivHList` для каждого класса типов, 
для которого мы хотим автоматически генерировать экземпляры для кейс-классов. 
Как только мы это сделаем, то сможем просто:

```dotty
case class Example[A, B](a: A, b: Seq[B], c: Int)

assert(Example('a', Seq(1, 2, 3), 19) === Example('a', Seq(1, 2, 3), 19))
assert(Example('b', Seq(1, 2, 3), 1) !== Example('a', Seq(1, 2, 3), 1))
```

В этом примере предполагается, что `===` и `!==` предоставляются неявным преобразованием, например:

```dotty
extension [A](a1: A)
  def ===(a2: A): Boolean = Equiv.universal.equiv(a1, a2)

  def !==(a2: A): Boolean = !Equiv.universal.equiv(a1, a2)
```

`Predef.conforms` также необходимо скрыть. 
В противном случае `equivHListable` расходится. Было бы хорошо иметь хорошее решение для этого.

И последнее замечание: даже если компилятор автоматически не генерирует преобразования, 
вручную определить преобразования в `HLists` и из них для каждого кейс-класса может быть легче, чем вручную реализовать класс типов. 
Вероятно, это тот случай, когда вы хотите реализовать несколько классов типов для одного класса.

В следующем и последнем разделе этой части будет рассказано о некоторых способах упростить работу со списками `HList` в Scala.

## Часть 6g. Type-indexed HLists

[Исходная статья][part 6g]

В [6c](type-level-programming-in-scala.md#часть-6c-индексирование-hlist) 
мы выполняли различные операции на основе позиции в `HList`, выбранной по натуральному числу уровня типа (`Nat`). 
В этом посте мы подбираем позиции по типам. 

Подобно 6c, мы берем `HList` и тип и создаем экземпляр `Indexed`, 
который предоставляет значение и тип по выбранному индексу, а также `HList` до и после индекса. 
Мы можем повторно использовать реализацию `Indexed`, определив несколько новых имплицитов.

В исходном подходе использовалась функция уровня типа 
для вычисления необходимого индексированного типа полностью на уровне типа:

```dotty
type toI[H <: HList, N <: Nat] <: Indexed
```

Для типа `HList` `HL` и `Nat` `N` `toI` предоставил индексированный тип для позиции, выбранной номером `N`. 
Затем мы могли бы потребовать неявный индексированный параметр типа `HL#toI[N]` и определить неявные значения, 
которые создают запрошенные индексированные значения.

Однако здесь это не сработает. 
Мы хотим выбрать позицию в `HList`, где тип ячейки `H` совпадает с запрошенным типом `S`. 
Мы не можем вычислить индексированный тип, используя решение только на уровне типа, 
поскольку не существует общей функции равенства на уровне типа. 
Нам нужно полагаться на неявное доказательство равенства двух типов, используя `=:=`. 
Более конкретно, вы не можете написать:

```dotty
type TypeEq[A, B] <: Bool
```

но вы можете потребовать доказательство (как значение) того, что `A` и `B` равны:

```dotty
def typeEq[A,B](implicit ev: A =:= B) ...
```

Помните из 6c, что `Indexed` состоит из двух частей: 
`Indexed0`, предоставляющий местоположение указателя в `HList`, 
и `IndexedN`, предоставляющий местоположения перед указателем. 
Чтобы выбрать позицию по типу, мы будем использовать класс-оболочку, 
которая записывает выбранный тип `S` и сопоставление `HList` с `Indexed`:

```dotty
sealed trait Tip[S, HL <: HList, Ind <: Indexed]:
  def apply(hl: HL): Ind
```

Таким образом, экземпляр `Tip` для `HList` `HL` и типа `S` может предоставить индексированный экземпляр, 
в котором мы можем вызывать такие методы, как `at`, `remove` или `map`.

Фактическая работа — создание экземпляров `Tip`. Существует один случай для `Indexed0` и один для `IndexedN`. 
Когда у нас есть доказательства того, что тип заголовка ячейки `HCons` совпадает с искомыми типом, 
то мы предоставляем значение `Indexed0`, которое указывает на эту ячейку. 
Опять же, мы обертываем ее в `Tip`, чтобы отметить, что мы выбрали ячейку типа `S`, 
и предоставить функцию, которая сопоставляет ячейку `HCons` с индексированным экземпляром, 
который нам в конечном итоге нужен.

```dotty
given tindexed0[S, H, T <: HList](using
    ev: S =:= H
): Tip[S, H :: T, Indexed0[H, T]] with
  def apply(hc: H :: T): Indexed0[H, T] = Indexed0[H, T](hc)
```

`Indexed0` указывает на заголовок выбранной ячейки `HCons` и ссылается на ее остаток. 
Затем нужно создать экземпляры `IndexedN` из завершающего `Indexed0`, чтобы ссылаться на ячейки перед выбранной. 
Для ячейки `HCons` типа `H :: T` мы требуем, чтобы тип `S` был найден в хвосте `T`. 
То есть нам нужен экземпляр `Tip` для хвостового типа `T`, искомый тип `S` и некоторый индексированный экземпляр `I`. 
Из этого мы можем предоставить подсказку для текущей ячейки.

```dotty
given tindexedN[H, T <: HList, I <: Indexed, S](using
    iTail: Tip[S, T, I]
): Tip[S, H :: T, IndexedN[H, I]] with
  def apply(hc: H :: T): IndexedN[H, I] =
    IndexedN[H, I](hc.head, iTail(hc.tail))
```

Обратите внимание, что это приведет к неоднозначным неявным значениям, если имеется несколько ячеек одного типа.

Чтобы связать вышеизложенное с `HList` для запрошенного типа, мы добавляем метод `t` в `HCons`:

```dotty
final case class HCons[H, T <: HList](head: H, tail: T) extends HList:
  self =>
  def t[S]: TipDummy[S, H :: T] = TipDummy[S, H :: T](self)
 
sealed class TipDummy[S, HL <: HList](val hl: HL)
```

где `H :: T` — тип ячейки `HCons`. 
Затем мы обеспечиваем неявное преобразование из `TipDummy` в `Indexed`:

```dotty
object TipDummy:
  given fromTipDummy[S, HL <: HList, I <: Indexed](using
      tip: Tip[S, HL, I]
  ): Conversion[TipDummy[S, HL], I] with
    def apply(dummy: TipDummy[S, HL]): I =
      tip(dummy.hl)
```

Промежуточный `TipDummy` выполняет применение параметра частичного типа. 
Мы хотим иметь возможность явно указывать тип для выбора без необходимости предоставления типов `Indexed` и `HList`. 

Примеры:

```dotty
val x = 3 :: true :: "asfd" :: 'k' :: () :: 9.3 :: HNil

// получение значения типа Boolean
val b2: Boolean = x.t[Boolean].toIndexed.at
// true

// удаление значений перед значением типа String
val pre = x.t[String].drop
// "asfd" :: 'k' :: () :: 9.3 :: HNil

// замена значения типа String на число 19
val rep = x.t[String].replace(19)
// 3 :: true :: 19 :: 'k' :: () :: 9.3 :: HNil

// замена символа Char на true если он в нижнем регистре, false - в ином случае
val mp = x.t[Char].map(_.isLower)
// 3 :: true :: "asfd" :: true :: () :: 9.3 :: HNil

// удаление значения типа Unit
val rm = x.t[Unit].remove
// 3 :: true :: "asfd" :: 'k' :: 9.3 :: HNil

// замена значения типа String на HList, построенный на её значениях
val fmp = x.t[String].flatMap(s => s.charAt(0) :: s.substring(1) :: HNil)
// 3 :: true :: 'a' :: "sfd" :: 'k' :: () :: 9.3 :: HNil

// вставка значения перед полем типа Int
val ins0 = x.t[Int].insert(List(3, 4))
// List(3, 4) :: 3 :: true :: "asfd" :: 'k' :: () :: 9.3 :: HNil

// вставка значения перед полем типа Double
val ins7 = x.t[Double].insert(-3.0f)
// 3 :: true :: "asfd" :: 'k' :: () :: -3.0 :: 9.3 :: HNil

// вставка HList перед значением типа String
val insH = x.t[String].insertH('h' :: b2 :: Some(3) :: None :: HNil)
// 3 :: true :: 'h' :: true :: Some(3) :: None :: "asfd" :: 'k' :: () :: 9.3 :: HNil

// разбиение HList перед значением с типом Unit
val (aa, bb) = x.t[Unit].splitAt
// (3 :: true :: "asfd" :: 'k' :: HNil, () :: 9.3 :: HNil)

// аналог операции удаление справа
type R = Double :: Unit :: Char :: String :: Boolean :: Int :: HNil.type
val dropRight = x.reverse.asInstanceOf[R].t[Char].drop.reverse
// 3 :: true :: "asfd" :: 'k' :: HNil
```

## Часть 7. Естественные литералы преобразования

[Исходная статья][part 7]

В исходной статье разбирается проблема определения [естественного преобразования](https://apocalisp.wordpress.com/2010/07/02/higher-rank-polymorphism-in-scala)?

В версии Scala, используемой в исходной статье, естественные преобразования вызывали проблемы.
Например, мы нельзя было определить функцию, которая сопоставляет `Option[T]` с `List[T]` для каждого `T`. 
Нельзя было определить `toList` так, чтобы компилировалось следующее:

```dotty
val toList = ...
 
val a: List[Int] = toList(Some(3))
assert(List(3) == a)

val b: List[Boolean] = toList(Some(true))
assert(List(true) == b)
```

Но в Scala 3 есть [полиморфные типы функций](../scala/type-system/polymorphic-function-types.md)
позволяющие определить функцию:

```dotty
val toList: [A] => Option[A] => List[A] =
  [A] => (opt: Option[A]) => opt.toList

val a: List[Int] = toList(Some(3))
assert(List(3) == a)

val b: List[Boolean] = toList(Some(true))
assert(List(true) == b)
```

## Часть 8a. Мотивация KList

[Исходная статья][part 8a]

В части 8 рассмотрим операции с кортежами произвольной арности над конструктором типа. 
Это разнородные списки высшего порядка, которые будем называть `KList`. 
Чтобы понять, почему нам может понадобиться такая структура, для простоты начнем с `Tuple2`.

Рассмотрим некоторые базовые преобразования элементов `Tuple2`. 
Если мы хотим применить одну и ту же функцию к обоим элементам, 
то можем потребовать, чтобы элементы имели общий супертип и чтобы функция работала с этим супертипом:

```dotty
def map1[A <: C, B <: C, C, D](t: (A, B), f: C => D): (D, D) =
  (f(t._1), f(t._2))

map1(("3", true), (_: Any).hashCode)
// (51, 1231)
```

Или мы можем предоставить две отдельные функции для независимой работы с каждым компонентом:

```dotty
def map2[A, B, C, D](t: (A, B), f: A => C, g: B => D): (C, D) =
  (f(t._1), g(t._2))

map2((1, true), (_: Int) + 1, !(_: Boolean))
// (2, false)
```

Теперь рассмотрим `Tuple2`, в котором типы обоих компонентов создаются одним и тем же конструктором типа, 
например `(List[Int], List[Boolean])` или `(Option[String], Option[Double])`.

Одна полезная операция над такой структурой выглядит так:

```dotty
def transform[F[_], A, B, G[_]](k: (F[A], F[B]), g: [C] => F[C] => G[C]): (G[A], G[B]) =
  (g(k._1), g(k._2))  
```

При этом предоставленное естественное преобразование применяется к каждому элементу, 
сохраняя базовый параметр типа, но с новым конструктором типа. 
В качестве примера можем применить преобразование `toList`, которое мы определили в части 7:

```dotty
val toList: [A] => Option[A] => List[A] =
  [A] => (opt: Option[A]) => opt.toList
  
// чтобы в качестве типа получать Option[T],
// а не Some[T] или None
def some[T](t: T): Option[T] = Some(t)
def none[T]: Option[T]       = None

transform[Option, Int, String, List]((some(3), some("str")), toList),
// (List(3), List("str"))

transform[Option, Boolean, Double, List]((some(true), none[Double]),toList),
// (List(true), List()) 
```

В части 6 мы занимались обобщением `TupleN` на случай произвольной арности. 
В части 8 мы обобщим операции преобразования и связанные с ними операции 
на разнородные списки с помощью конструктора типов.

## Часть 8b. Основы KList

[Исходная статья][part 8b]

При отсутствии типов ранга 2 может быть полезно иметь разнородный список более высокого порядка, который здесь назовем `KList`. 
`KList` определяет конструктор типа `M[_]`, который используется для создания типа для всех ячеек в списке. 
Параметр, передаваемый в конструктор этого типа, может быть разным для каждой ячейки, которая является разнородной частью. 
Одним из вариантов использования `KList` является определение общей функции `zipWith`. 
`KList`-ы также используются при реализации механизма задач в **sbt**. 
Каждое из этих приложений будет описано в последующих постах.

Начнем с базового определения `KList`, которое выглядит так:

```dotty
sealed trait KList[+M[_], HL <: HList]

final case class KCons[H, T <: HList, +M[_]](head: M[H], tail: KList[M, T])
    extends KList[M, H :: T]:
  self =>
  // добавление в начало
  def :^:[N[X] >: M[X], G](g: N[G]): KCons[G, H :: T, N] = KCons(g, self)

sealed class KNil extends KList[Nothing, HNil.type]:
  def :^:[M[_], H](h: M[H]): KCons[H, HNil.type, M] = KCons(h, this)

object KNil extends KNil

object KList:
  // более удобный псевдоним для сопоставления с образцом
  val :^: : KCons.type = KCons
```

Он похож на `HList`, за исключением конструктора типа `M`. 
Мы сохраняем тип `head`-а в `KCons` двумя частями: конструктор типа `M`, общий для всех ячеек в `KList`, 
и параметр типа `M`, специфичный для этой ячейки. Полный тип заголовка тогда будет `M[H]`.

Пример конструкции:

```dotty
val m = List(1, 2, 3, 4) :^: List("str1", "str2") :^: KNil
```

Тип переменной:

```dotty
KCons[Int, java.lang.String :: HNil, List]
```

Обратите внимание, что мы можем смешивать совместимые конструкторы типов:

```dotty
val m = Seq(1, 2, 3, 4) :^: List("str1", "str2") :^: KNil
// KCons[Int, java.lang.String :: HNil, Seq]
```

Те, которые несовместимы, приводят к сбою компилятора:

```dotty
final case class MyArray(str: String, str1: String)

val m = Seq(1, 2, 3, 4) :^: MyArray("str1", "str2") :^: KNil
// Found: KListSuite.this.MyArray Required: M[H]  
```

В некоторых случаях невозможно вывести типы, например, когда конструктор типа — `Id`, где `type Id[X] = X`:

```dotty
// не скомпилируется
val p = 1 :^: true :^: KNil
```

Ключевое использование `KList` — применение естественного преобразования к своему содержимому. 
Мы сохранили конструктор типа отдельно от параметров типа, что означает, 
что мы можем применить естественное преобразование `[A] => M[A] => N[A]` к каждому элементу 
и сохранить базовые параметры типа. В качестве примера рассмотрим наш разнородный список списков:

```dotty
val m = List(1, 2, 3, 4) :^: List("str1", "str2") :^: KNil
```

и естественное преобразование, которое принимает `List` и вызывает для него `headOption`:

```dotty
val headOption: [A] => List[A] => Option[A] =
  [A] => (list: List[A]) => list.headOption
```

Затем применяем `headOption` к `m`:

```dotty
val heads = m transform headOption
// KCons(Some(1), KCons(Some("str1"), KNil))
```

Мы получаем список опций `KList`, сохраняя информацию о том, 
что первый элемент имеет тип `Option[Int]`, а второй — тип `Option[String]`.

Метод `transform` в `KList` реализовать просто:

```dotty
sealed trait KList[+M[_], HL <: HList]:
  def transform[N[_]](f: [A] => M[A] => N[A]): KList[N, HL]
  
final case class KCons[H, T <: HList, +M[_]](head: M[H], tail: KList[M, T])
    extends KList[M, H :: T]:
  ...
  def transform[N[_]](f: [A] => M[A] => N[A]): KList[N, H :: T] =
    KCons(f(head), tail transform f)  

sealed class KNil extends KList[Nothing, HNil.type]:
  ...
  def transform[N[_]](f: [A] => Nothing => N[A]): KList[N, HNil.type] = KNil    
```

Мы можем добавить еще один метод, который преобразует `KList` в базовый `HList`. 
Например, мы могли бы уменьшить каждый список в нашем `KList` выше до его головного элемента:

```dotty
val head: [A] => List[A] => Id[A] =
  [A] => (list: List[A]) => list.head

val heads = m down head
// 1 :: "str1" :: HNil
```

Определение метода `down` выглядит так:

```dotty
sealed trait KList[+M[_], HL <: HList]:
  // Для преобразования KList в HList
  def down(f: [A] => M[A] => Id[A]): HL
  
final case class KCons[H, T <: HList, +M[_]](head: M[H], tail: KList[M, T])
    extends KList[M, H :: T]:
  ...
  def down(f: [A] => M[A] => Id[A]) = HCons(f(head), tail down f)

sealed class KNil extends KList[Nothing, HNil.type]:
  ...
  def down(f: [A] => Nothing => Id[A]) = HNil
```

В следующем разделе мы будем использовать `down` и `transform`, 
чтобы реализовать `zipWith` для произвольной арности.

## Часть 8c. KList ZipWith

[Исходная статья][part 8c]

Одним из вариантов использования `KList` и методов `transform` и `down` 
является реализация таких методов, как `zipWith`, для кортежей произвольной длины. 
Начнем с того, что подпись `zipWith` для пары потоков, работающих с фиксированной арностью `2`, выглядит так:

```dotty
def zipWith2[A, B, C](t2: (LazyList[A], LazyList[B]))(
    f: (A, B) => C
): LazyList[C] =
  t2 match
    case (ha #:: ta, hb #:: tb) =>
      LazyList.cons(f(ha, hb), zipWith2((ta, tb))(f))
    case _ => LazyList.empty
```

Пример использования:

```dotty
val nats   = LazyList.from(1)
val random = LazyList.continually(math.random)
val seq    = zipWith2((nats, random)) { (n, r) => if (r > 0.3) n else -n }

seq.take(10).toList
// List(-1, -2, 3, 4, 5, -6, 7, 8, -9, 10)
```

Для реализации `zipWith2`, если любой из потоков в паре пуст, результирующий поток также будет пуст. 
В противном случае для каждого потока в паре существует элемент `head`. 
Мы применяем предоставленную функцию к этим элементам и делаем результат заголовком нового потока. 
Остаток этого нового потока будет результатом рекурсии остатков входной пары.

Чтобы обобщить код до произвольной арности, мы будем работать со списком потоков `KList`. 
Поскольку мы хотим абстрагироваться от арности, то используем разнородный список. 
Мы используем `KList` вместо `HList`, потому что хотим, чтобы каждый элемент в кортеже был потоком, 
и нас не волнует, к какому конкретному типу потоков относятся элементы, но мы хотим сохранить эти типы. 
Когда мы берем элемент `head` каждого потока, результирующий список является базовым типом `HList` входного `KList`. 
Например, учитывая ввод типа `KList[LazyList, A :: B :: HNil]`, когда мы берем заголовок каждого потока в `KList`, 
то получаем `HList` типа `A :: B :: HNil`. 
Это похоже на переход от `(LazyList[A], LazyList[B])` к `(A, B)`.

Итак, если мы в конечном итоге получим базовый тип `HList`, 
функция, которую мы применим к входному `KList`, должна быть функцией от этого типа `HList` к какому-либо другому типу. 
В приведенном выше примере тип функции будет относиться `A :: B :: HNil => T` к некоторому типу `T`, 
который будет типом выходного потока. Таким образом, у нас есть подпись для обобщенного `zipWith`:

```dotty
def zipWith[HL <: HList, T](kl: KList[LazyList, HL])(f: HL => T): LazyList[T]
```

Чтобы реализовать эту функцию, мы снова разобьем задачу на две части. 
Если какой-либо поток пуст, результирующий поток также будет пуст. 
В противном случае мы получаем все элементы `head` `LazyList`-ов в виде `HList`, 
применяем к нему функцию ввода и делаем его новым `head`. 
Для остатка получаем все остатки потоков и выполняем рекурсию. 
Чтобы получить элементы `head`, мы используем `down`, потому что нам нужно `KList[LazyList, HL] => HL`. 
Для остатков используем `transform`, потому что нам нужно отображение `KList[LazyList, HL] => KList[LazyList, HL]`. 
Реализация выглядит так:

```dotty
def zipWith[HL <: HList, T](
    kl: KList[LazyList, HL]
)(f: HL => T): LazyList[T] =
  if (anyEmpty(kl))
    LazyList.empty
  else
    LazyList.cons(f(kl down heads), zipWith(kl transform tails)(f))

def anyEmpty(kl: KList[LazyList, _]): Boolean =
  kl match
    case KCons(head, tail) => head.isEmpty || anyEmpty(tail)
    case _                 => false

val heads: [A] => LazyList[A] => Id[A] =
  [A] => (list: LazyList[A]) => list.head

val tails: [A] => LazyList[A] => LazyList[A] =
  [A] => (list: LazyList[A]) => list.tail
```

В `zipWith` мы можем вызвать метод `isEmpty` для элементов списка, 
но мы не получим конкретный тип, если, например, вызовем `head`. 
"Заголовки" и "остатки" — это естественные преобразования, 
которые сопоставляют `LazyList[T]` с его началом типа `T` и остатком типа `LazyList[T]` соответственно.

Исходный пример, переведенный для использования обобщенного `zipWith`, выглядит так:

```dotty
???
val nats = Stream.from(1)
val random = Stream.continually( math.random )
val seq = zipWith( nats :^: random :^: KNil ) {
   case n :: r :: HNil => if(r > 0.3) n else -n
}
 
scala> seq.take(10).toList
res0: List[Int] = List(-1, 2, -3, -4, -5, 6, 7, 8, 9, 10)
???
```

Мы можем реализовать соответствующую функцию `zip` в терминах `zipWith`.

```dotty
???
def zipped[HL <: HList](kl: KList[Stream, HL]): Stream[HL] =
   zipWith(kl)(x => x)
???
```

Или мы могли бы реализовать `zipWith` в терминах `zip`. 
В любом случае мы можем реализовать несколько других функций, используя `zip`:

```dotty
???
def foreach[HL <: HList, T](kl: KList[Stream, HL])(f: HL => T): Unit =
   zipped(kl).foreach(f)
 
def collect[HL <: HList, T](kl: KList[Stream, HL])(f: HL => Option[T]): Stream[T] =
   zipped(kl).collect(f)
 
def flatMap[HL <: HList, T](kl: KList[Stream, HL])(f: HL => Stream[T]): Stream[T] =
   zipped(kl).flatMap(f)
 
def forall[HL <: HList](kl: KList[Stream, HL])(f: HL => Boolean): Boolean =
   zippped(kl).forall(f)
       
def exists[HL <: HList](kl: KList[Stream, HL])(f: HL => Boolean): Boolean =
   zipped(kl).exists(f)
???
```

Пример использования `foreach`:

```dotty
???
val a = Stream(1,2,5,3,9,10,101)
val b = Stream("one", "two", "three", "four")
val c = Stream(true, false, false, true, true)
 
zipped(a :^: b :^: c :^: KNil) foreach {
   case n :: s :: b :: HNil =>
      println( s * (if(b) 1 else n) )
}
 
one
twotwo
threethreethreethreethree
four
???
```

---

**Ссылки:**

- [Исходный код][source]
- [Исходный код тестов](https://gitflic.ru/project/artemkorsakov/scalabook/file?file=examples%2Fsrc%2Ftest%2Fscala%2Fru%2Fscalabook%2Fbooks%2Ftlpis&branch=master)
- ["Type-Level Programming in Scala"][blog]
    - [Part 1: Type Recursion][part 1]
    - [Part 2: implicitly and =:=][part 2]
    - [Part 3: Booleans][part 3]
    - [Part 4a: Peano number basics][part 4a]
        - [Peano numbers][wiki]
    - [Part 4b: Comparing Peano numbers][part 4b]
    - [Part 4c: General recursion on Peano numbers][part 4c]
    - [Part 4d: Peano arithmetic][part 4d]
    - [Part 5a: Binary numbers][part 5a]
    - [Part 5b: Project Euler problem 4][part 5b]
    - [Part 6a: Heterogeneous List Basics][part 6a]
    - [Part 6b: HList folds][part 6b]
    - [Part 6c: HList Indexing][part 6c]
    - [Part 6d: HList Zip/Unzip][part 6d]
    - [Part 6e: HList Apply][part 6e]
    - [Part 6f: Deriving type class instances through HLists][part 6f]
    - [Part 6g: Type-indexed HLists][part 6g]
    - [Part 7: Natural transformation literals][part 7]
    - [Part 8a: KList motivation][part 8a]
    - [Part 8b: KList basics][part 8b]
    - [Part 8c: KList ZipWith][part 8c]
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
[part 5a]: https://apocalisp.wordpress.com/2010/06/24/type-level-programming-in-scala-part-5a-binary-numbers/
[part 5b]: https://apocalisp.wordpress.com/2010/06/24/type-level-programming-in-scala-part-5b-project-euler-problem%C2%A04/
[part 6a]: https://apocalisp.wordpress.com/2010/07/06/type-level-programming-in-scala-part-6a-heterogeneous-list%C2%A0basics/
[part 6b]: https://apocalisp.wordpress.com/2010/07/08/type-level-programming-in-scala-part-6b-hlist%C2%A0folds/
[part 6c]: https://apocalisp.wordpress.com/2010/07/12/type-level-programming-in-scala-part-6c-hlist%C2%A0indexing/
[part 6d]: https://apocalisp.wordpress.com/2010/07/17/type-level-programming-in-scala-part-6d-hlist%C2%A0zipunzip/
[part 6e]: https://apocalisp.wordpress.com/2010/10/15/type-level-programming-in-scala-part-6e-hlist%C2%A0apply/
[part 6f]: https://apocalisp.wordpress.com/2010/10/18/type-level-programming-in-scala-part-6f-deriving-type-class-instances-through%C2%A0hlists/
[part 6g]: https://apocalisp.wordpress.com/2010/10/22/type-level-programming-in-scala-part-6g-type-indexed%C2%A0hlists/
[part 7]: https://apocalisp.wordpress.com/2010/10/26/type-level-programming-in-scala-part-7-natural-transformation%C2%A0literals/
[part 8a]: https://apocalisp.wordpress.com/2010/11/01/type-level-programming-in-scala-part-8a-klist%C2%A0motivation/
[part 8b]: https://apocalisp.wordpress.com/2010/11/03/type-level-programming-in-scala-part-8b-klist%C2%A0basics/
[part 8c]: https://apocalisp.wordpress.com/2010/11/15/type-level-programming-in-scala-part-8c-klist%C2%A0zipwith/
[wiki]: https://wiki.haskell.org/Peano_numbers
[match types]: ../scala/type-system/match-types.md
[Project Euler]: https://projecteuler.net/about
[Project Euler, p4]: https://projecteuler.net/index.php?section=problems&id=4
