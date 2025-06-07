{%
showPrev = true
prevPage = handling-errors.html
showNext = true
nextPage = laziness.html
%}

# For-comprehension

## Введение

For-comprehension — это мощный синтаксический сахар в Scala, 
позволяющий работать [с монадическими типами](../typeclass/monad/monad.md). 
Под капотом for-comprehension преобразуется в цепочку вызовов `map`, `flatMap` и `filter`. 
Этот механизм особенно полезен при работе с коллекциями, `Option`, `Try`, `Future` 
и другими типами, реализующими `map`, `flatMap` и `filter`.

## Базовый синтаксис

Простейший for-comprehension выглядит так:

```dotty
val result =
  for
    x <- List(1, 2, 3)
  yield x * 2
// Результат: List(2, 4, 6)
```

Что эквивалентно вызову:

```dotty
List(1, 2, 3).map(x => x * 2)
// Результат: List(2, 4, 6)
```

## Множественные генераторы

For-comprehension становится особенно полезным при работе с несколькими генераторами:

```dotty
val pairs =
  for
    a <- List(1, 2)
    b <- List('a', 'b')
  yield (a, b)
// Результат: List((1,'a'), (1,'b'), (2,'a'), (2,'b'))
```

Эквивалентный код без for-comprehension:

```dotty
List(1, 2).flatMap(a => List('a', 'b').map(b => (a, b)))
// Результат: List((1,'a'), (1,'b'), (2,'a'), (2,'b'))
```

## Фильтрация

В for-comprehension можно использовать условия фильтрации с помощью `if`:

```dotty
val evenSquares =
  for
    x <- 1 to 10
    if x % 2 == 0
  yield x * x
// Результат: Vector(4, 16, 36, 64, 100)
```

## Работа с Option

For-comprehension отлично подходит для работы с `Option`, позволяя избегать вложенных проверок на `None`:

```dotty
def getUser(id: Int): Option[String]  = ???
def getAge(name: String): Option[Int] = ???

val result =
  for
    name <- getUser(123)
    age  <- getAge(name)
  yield s"$name is $age years old"
// Результат: Option[String]
```

## Обработка ошибок с Try

Аналогично можно работать с `Try`:

```dotty
import scala.util.Try

val result =
  for
    a <- Try("123".toInt)
    b <- Try("456".toInt)
  yield a + b
// Success(579)  
```

## Под капотом

Scala преобразует for-comprehension по следующим правилам:

1. Каждый `<-` становится `flatMap`
2. Последний `yield` становится `map`
3. Условия `if` превращаются в `withFilter`

## Ограничения

Не все типы можно использовать в for-comprehension. 
Тип должен поддерживать:

- `map` и `flatMap` — для генераторов
- `withFilter` — для условий `if`

## Заключение

For-comprehension — это элегантный способ работы со структурами данных, реализующих `map`/`flatMap`, который:

- Улучшает читаемость кода
- Уменьшает количество вложенных вызовов
- Поддерживает единый стиль работы

Рекомендуется использовать for-comprehension вместо цепочек `map`/`flatMap`, 
когда операций становится больше двух или появляется необходимость в фильтрации.

## Упражнения

### Упражнение 4.1.1: Вложенные структуры

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

Дан список списков (матрица 3x2):

```dotty
val matrix = List(List(1, 2), List(3, 4), List(5, 6))  
```

Напишите for-comprehension, который преобразует эту матрицу в одномерный список, извлекая все элементы по порядку.

**Ожидаемый результат**:

```dotty
List(1, 2, 3, 4, 5, 6)
```  

@:note(🔍 Показать решение)

Этот for-comprehension преобразует вложенный список `matrix` в плоский, используя два генератора: 
первый (`row <- matrix`) последовательно извлекает каждый подсписок, 
а второй (`num <- row`) проходит по элементам текущего подсписка, 
после чего `yield num` собирает все элементы в одномерный список. 

```dotty
for
  // Итерация по внешнему списку 
  // (получаем List(1,2), затем List(3,4) и т.д.)
  row <- matrix  
  // Итерация по каждому внутреннему списку   
  num <- row   
// Возвращаем каждый элемент по отдельности       
yield num           
```

@:@

### Упражнение 4.1.2: Собственный тип для for-comprehension

Дан класс:

```dotty
case class Box[A](value: A)  
```

Сделайте так, чтобы с `Box` можно было использовать for-comprehension. 
Для этого необходимо реализовать методы `map` и `flatMap`, 
которые являются частью контракта for-comprehension в Scala.

**Пример**:

```dotty
val result =
  for
    a <- Box(2)
    b <- Box(3)
  yield a + b
// Ожидаемый результат: Box(5)
```  

@:note(🔍 Показать решение)

Для работы for-comprehension с типом `Box[A]` необходимо реализовать:

1. `map` - преобразует значение внутри `Box` с помощью функции `A => B`
2. `flatMap` - позволяет последовательно комбинировать операции, сохраняя контекст `Box`

```dotty
case class Box[A](value: A):
  def map[B](f: A => B): Box[B] = 
    Box(f(value))
  def flatMap[B](f: A => Box[B]): Box[B] = 
    f(value)
```

Объяснение:

- `map` создает новый `Box`, применяя функцию `f` к значению
- `flatMap` применяет функцию, которая уже возвращает `Box`, и "распаковывает" результат
- Этих двух методов достаточно для базового for-comprehension (без фильтрации)

@:@

### Упражнение 4.1.3: Совместимость типов в for-comprehension

Рассмотрим два варианта кода:

**Вариант 1**:

```dotty
val a = Option(1)
val b = List(1, 2, 3)

val c =
  for
    x <- a  // Генератор Option
    y <- b  // Генератор List
  yield x + y
```

**Вариант 2** (поменяли порядок генераторов):

```dotty
val c =
  for
    y <- b  // Генератор List
    x <- a  // Генератор Option
  yield x + y
```

**Вопросы**:

1. Какой тип и значение будет у `c` в первом варианте?
2. Какой тип и значение будет у `c` во втором варианте?

@:note(🔍 Показать решение)

**Ответ 1**: Первый вариант **не скомпилируется**.

**Причина**:

- Эквивалентный код: `a.flatMap(x => b.map(y => x + y))`
- `Option.flatMap` ожидает функцию `A => Option[B]`, но `b.map` возвращает `List[Int]`
- Нет неявного преобразования `List` → `Option`

**Ответ 2**: Второй вариант даст `List(2, 3, 4)`.

**Причина**:

- Эквивалентный код: `b.flatMap(y => a.map(x => x + y))`
- `List.flatMap` ожидает `A => IterableOnce[B]`
- `Option` неявно преобразуется в `IterableOnce`
- Результат: для каждого `y` из списка берётся `x` из `Option(1)`

**Дополнительные вопросы**:

Как сделать первый вариант рабочим (с `Option` первым)?

**Ответ 3**: явно преобразовать `Option` в коллекцию

```dotty
val c =
  for
    x <- a.toList
    y <- b
  yield x + y
```

@:@

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]

[sample]: https://gitverse.ru/artemkorsakov_scalabook/functional_programming_sample
