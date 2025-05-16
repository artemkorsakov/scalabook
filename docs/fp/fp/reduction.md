{%
showPrev = true
prevPage = pure_function.html
showNext = true
nextPage = higher_order_function.html
%}

# Редукция

## Понятие редукции

_Редукция_ (или _сокращение_, от англ. _reduction_) — это процесс последовательного преобразования сложного выражения
к его простейшей эквивалентной форме через применение правил преобразования.
Этот процесс аналогичен математическим вычислениям, где мы постепенно упрощаем выражения до их окончательного значения.

## Пример редукции на практике

Рассмотрим детальный разбор вычисления выражения `square(3 + 4)`:

1. **Первая стратегия вычислений** (внутреннее сокращение): `square(3 + 4) => square(7) => 7 * 7 => 49`
2. **Альтернативный подход** (внешнее сокращение): `square(3 + 4) => (3 + 4) * (3 + 4) => 7 * (3 + 4) => 7 * 7 => 49`

Ключевые наблюдения:

- Оба пути приводят к одинаковому результату (49)
- Конечное выражение `49` является _канонической формой_ (нормальной формой) — его нельзя сократить дальше
- Порядок применения редукций может различаться, но сохраняется свойство _конфлюэнтности_ ([Church-Rosser property](https://ru.wikipedia.org/wiki/%D0%A2%D0%B5%D0%BE%D1%80%D0%B5%D0%BC%D0%B0_%D0%A7%D1%91%D1%80%D1%87%D0%B0_%E2%80%94_%D0%A0%D0%BE%D1%81%D1%81%D0%B5%D1%80%D0%B0))

## Формальная система выражений

Рассмотрим формальный язык для представления целых чисел с операциями:

- Базовый случай: `zero` — атомарное выражение
- Рекурсивные случаи:
    - `(succ e)` — следующее число (_successor_)
    - `(pred e)` — предыдущее число (_predecessor_)

**Правила редукции**:

1. `(succ (pred e)) => e`
2. `(pred (succ e)) => e`

## Алгоритм нормализации выражений

Пошаговый процесс упрощения выражения `(succ(pred(succ(pred(pred zero)))))`:

1. **Первая итерация**:
   Находим вложенную пару `succ(pred ...)`:
   ```
   (succ (pred (succ (pred (pred zero)))))
   => (succ (pred (pred zero)))
   ```
2. **Вторая итерация**:
   Применя то же правило:
   ```
   (succ (pred (pred zero)))
   => (pred zero)
   ```
3. **Результат**:
   Выражение `(pred zero)` не содержит сократимых пар `succ/pred`, следовательно:
    - Это каноническая форма
    - Дальнейшая редукция невозможна

## Реализация на Scala 3

```dotty
enum Expr:
  self =>

  case Zero
  case Succ(expr: Expr)
  case Pred(expr: Expr)

  def normalize: Expr =
    self match
      case Succ(Pred(e)) => e.normalize // Правило 1
      case Pred(Succ(e)) => e.normalize // Правило 2
      case Succ(e)       => Succ(e.normalize) // Рекурсивный случай
      case Pred(e)       => Pred(e.normalize) // Рекурсивный случай
      case _             => self // Базовый случай

import Expr.*
val expr = Succ(Pred(Succ(Pred(Pred(Zero)))))
expr.normalize // Результат: Pred(Zero)
```

## Значение редукции в функциональном программировании

Вот несколько причин, почему редукция применима в функциональном программировании:

- **Чистые функции**: Функциональный язык программирования опирается на чистые функции,
  которые не имеют побочных эффектов и возвращают одно и то же значение при одинаковых входных данных.
  Это делает возможным использование редукции, потому что каждая функция может быть сведена к своему значению независимо от контекста.
- **Рефакторинг и оптимизация**: Благодаря тому, что функции могут быть сведены к своим значениям,
  код становится легче рефакторить и оптимизировать.
  Например, можно заменить сложные выражения простыми значениями, не изменяя поведение программы.
- **Простота анализа и проверки корректности**: Поскольку каждое выражение может быть сведено к своему значению,
  анализ программы становится проще. Можно проверить корректность программы,
  рассматривая каждую функцию отдельно и убеждаясь, что она возвращает правильное значение.

## Упражнение 1.3.1: Редукция арифметических выражений

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

Реализуйте функцию `reduce` для пошаговой редукции арифметических выражений.
Выражения представлены следующим АДТ:

```dotty
enum Expr:
  case Num(value: Int)
  case Add(left: Expr, right: Expr)
  case Mul(left: Expr, right: Expr)
  case Var(name: String)
```

**Требования:**
1. Функция должна возвращать `Option[Expr]` (None если редукция невозможна)
2. Приоритет операций: умножение перед сложением
3. Редукция по шагам (одно преобразование за вызов)
4. Поддерживать частичную редукцию при наличии переменных

Примеры редукции:
```scala
Add(Num(2), Add(Num(3), Num(4)))  // => Some(Add(Num(2), Num(7)))
Mul(Num(2), Var("x"))             // => None (нельзя сократить)
Add(Num(1), Mul(Num(2), Num(3)))  // => Some(Add(Num(1), Num(6)))
```

```scala
def reduce(expr: Expr): Option[Expr] = ???
```

@:note(🔎 Детальный разбор решения 🔍)

**Алгоритм решения:**
1. Сначала пытаемся редуцировать умножения:
   ```scala
   case Mul(Num(a), Num(b)) => Some(Num(a * b))
   ```
2. Затем - сложения:
   ```scala
   case Add(Num(a), Num(b)) => Some(Num(a + b))
   ```
3. Для вложенных выражений применяем редукцию рекурсивно:
   ```scala
   case Add(e1, e2) => reduce(e1).map(Add(_, e2))
                      .orElse(reduce(e2).map(Add(e1, _)))
   ```
4. Переменные и полностью редуцированные выражения не сокращаются:
   ```scala
   case Var(_) | Num(_) => None
   ```

**Полное решение:**
```scala
def reduce(expr: Expr): Option[Expr] = expr match
  case Expr.Num(_) | Expr.Var(_) => None
  
  case Expr.Mul(Expr.Num(a), Expr.Num(b)) => Some(Expr.Num(a * b))
  case Expr.Mul(e1, e2) =>
    reduce(e1).map(Expr.Mul(_, e2))
      .orElse(reduce(e2).map(Expr.Mul(e1, _)))
  
  case Expr.Add(Expr.Num(a), Expr.Num(b)) => Some(Expr.Num(a + b))
  case Expr.Add(e1, e2) =>
    reduce(e1).map(Expr.Add(_, e2))
      .orElse(reduce(e2).map(Expr.Add(e1, _)))
```

**Примеры работы:**

```scala
reduce(Add(Num(1), Mul(Num(2), Num(3)))) 
// Some(Add(Num(1), Num(6)))

reduce(Mul(Var("x"), Add(Num(1), Num(2))))
// Some(Mul(Var("x"), Num(3)))

reduce(Add(Var("y"), Var("z")))
// None
```

**Критерии проверки:**
1. Корректность порядка редукции (сначала умножение)
2. Частичная редукция сложных выражений
3. Сохранение структуры при наличии переменных
4. Однократное применение правил за вызов

@:@


---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Харрисон - Введение в функциональное программирование](https://funprog-ru.github.io/)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)

[sample]: https://gitverse.ru/artemkorsakov_scalabook/functional_programming_sample
