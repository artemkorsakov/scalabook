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

Код все еще необходимо преобразовать в лучшее программирование на уровне целевого типа. 
По сути, утверждения и сравнения "if" могут быть проблематичными. 
См. Expanded объект в [Euler4.scala](https://github.com/harrah/up/blob/master/Euler4.scala) 
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

Затем мы хотим использовать `Bool#If` для обработки результата `isLargerPalindrome`. 
К сожалению, хотя до этого момента все работало нормально, как только мы начинаем создавать остальную часть программы, 
наша информация о типе теряется. Нам нужно сделать что-то менее простое. 
Нам нужно передать, что возвращать, если результат истинен или ложен. 
Грубо говоря, мы встраиваем `If`, `gt` и `&&`.

```dotty
```



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
    - [Part 5a: Binary numbers][part 5a]
    - [Part 5b: Project Euler problem 4][part 5b]
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
[wiki]: https://wiki.haskell.org/Peano_numbers
[match types]: ../scala/type-system/match-types.md
[Project Euler]: https://projecteuler.net/about
[Project Euler, p4]: https://projecteuler.net/index.php?section=problems&id=4
