{%
showPrev = true
prevPage = bothable.html
showNext = true
nextPage = ../other.html
%}

# Enum

**Типовой класс `Enum`**, расширяющий [`Order`](order.md) и [`Bothable`](bothable.md), — это абстракция, 
которая представляет типы с конечным набором значений, имеющих определённый порядок. 
Он сочетает в себе свойства перечислений (как в `Enum` из других языков) 
и возможность сравнения значений (благодаря расширению `Order`). 
Это позволяет работать с такими типами как с упорядоченными множествами, 
где каждое значение имеет чётко определённое положение относительно других.

**Где применяется?**

- **Работа с перечислениями**:
    - Для типов, которые представляют фиксированный набор значений (например, дни недели, месяцы, статусы заказов).
- **Сравнение и сортировка**:
    - Благодаря расширению `Order`, значения типа `Enum` можно сравнивать и сортировать.
- **Генерация последовательностей**:
    - `Enum` позволяет легко генерировать последовательности значений (например, все дни недели или все месяцы года).
- **Преобразование между значениями и их порядковыми номерами**:
    - Полезно при сериализации/десериализации, когда значения нужно сохранять в виде чисел.

**Примеры:**

- **Дни недели**:
    - Тип `DayOfWeek` может быть экземпляром `Enum`, где значения — `Monday`, `Tuesday`, ..., `Sunday`. 
      Метод `succ` для `Monday` вернёт `Tuesday`, а `pred` для `Sunday` вернёт `Saturday`.
- **Месяцы года**:
    - Тип `Month` может быть экземпляром `Enum`, где значения — `January`, `February`, ..., `December`. 
- **Статусы заказов**:
    - Тип `OrderStatus` может быть экземпляром `Enum`, где значения — `Pending`, `Processing`, `Shipped`, `Delivered`. 
- **Игральные карты**:
    - Тип `Suit` (масть) может быть экземпляром `Enum`, где значения — `Hearts`, `Diamonds`, `Clubs`, `Spades`. 

## Формальное определение

Члены типа `Enum`, расширяющего тип [`Order`](order.md) и [`Bothable`](bothable.md), 
представляют собой последовательно упорядоченные типы — их можно перечислить.

Основное преимущество класса типов `Enum` заключается в том, что мы можем использовать его типы в диапазонах списков. 
У них также есть определенные преемники (_successors_) и предшественники (_predecessors_), 
которые вы можете получить с помощью функций `succ` и `pred`.
И возможно существование минимального и максимального элементов.

`Enum` должен удовлетворять следующим законам (помимо законов `Order` и `Equal`):

- следующий предыдущего элемента `x` является `x`: `succ(pred(x)) == x`
- предыдущий следующего элемента `x` является `x`: `pred(succ(x)) == x`
- если определены и `min`, и `max`, то предыдущий от `min` - это `max`
- если определены и `min`, и `max`, то следующий для `max` - это `min`
- для любого x, не равного `max`, его следующий элемент больше или равен `x`: `x == max || succ(x) >= x`
- для любого x, не равного `min`, его предыдущий элемент меньше или равен `x`: `x == min || pred(x) <= x`

## Определение в виде кода на Scala

```dotty
trait Equal[A]:
  def equal(a1: A, a2: A): Boolean

enum Ordering:
  case LT
  case EQ
  case GT

trait Order[A] extends Equal[A]:
  def order(x: A, y: A): Ordering

trait Predecessible[A]:
  def pred(a: A): Option[A]

trait Successible[A]:
  def succ(a: A): Option[A]

trait Bothable[A] extends Predecessible[A], Successible[A]

trait Enum[A] extends Order[A], Bothable[A]:
  def min: Option[A]
  def max: Option[A]
```

## Схема

```mermaid
classDiagram
    class Equal~A~{
        +equal(x: A, y: A) Boolean
    }
    class Ordering{ 
        <<enumeration>> 
        LT 
        EQ 
		GT
    }   
    class Order~A~{
        +order(x: A, y: A) Ordering
    }
    Ordering .. Order 
    Equal <|-- Order
    class Bounded~A~{
        +min: A
        +max: A
    }
    Order <|-- Bounded
    class Sign{ 
        <<enumeration>> 
        Positive 
        Zero 
		Negative
    }   
    class Signed~A~{
        +sign(x: A) Sign
    }
    Sign .. Signed
    Order <|-- Signed 
    class Predecessible~A~{
        +pred(x: A) A
    }
    class Successible~A~{
        +succ(x: A) A
    }
    class Bothable~A~
    Predecessible <|-- Bothable 
    Successible <|-- Bothable     
    class Enum~A~{
        +min: Option[A]
        +max: Option[A]
    }
    Order <|-- Enum 
    Bothable <|-- Enum  
    Bounded .. Enum    
```

## Реализация в библиотеках

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*
1 |-> 2        // [1,2]
1 |--> (2, 5)  // [1,3,5]
1.pred         // 0
1.predx        // Some(0)
1.succ         // 2
1.succx        // Some(2)
Enum[Int].min  // Some(-2147483648)
Enum[Int].max  // Some(2147483647)
```

---

**Ссылки:**

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Enum.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Enum.html)
