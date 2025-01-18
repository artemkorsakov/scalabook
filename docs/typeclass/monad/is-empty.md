# IsEmpty

## Формальное определение

`IsEmpty` - класс типов, позволяющий проверить, действительно ли какой-либо тип с пустым представлением является пустым.
Расширяет [`PlusEmpty`](plus-empty.md).

`IsEmpty` должен удовлетворять следующим законам (помимо законов родителей): 

- `isEmpty` с параметром `empty` должен возвращать `true`: `isEmpty(empty[A]) == true`.
- `isEmpty` от композиции двух функторов равно `true` тогда и только тогда, когда `isEmpty` от каждого функтора - `true`: 
  `isEmpty(f1) && isEmpty(f2) == isEmpty(plus(f1, f2))`


## Код

```dotty
trait IsEmpty[F[_]] extends PlusEmpty[F]:
  def isEmpty[A](fa: F[A]): Boolean
```

### Связанный список

```dotty
given IsEmpty[List] with
  override def isEmpty[A](fa: List[A]): Boolean = fa.isEmpty
  override def plus[A](fa1: List[A], fa2: => List[A]): List[A] = fa1 ++ fa2
  override def empty[A]: List[A] = List.empty[A]
```

## Схема

```mermaid
classDiagram
    class Plus~A~{
        +plus[A](fa1: F[A], fa2: => F[A]) F[A]
    }
    class PlusEmpty~A~{
        +empty[A]: F[A]
    }
    Plus <|-- PlusEmpty
    class IsEmpty~A~{
        +isEmpty[A](fa: F[A]) Boolean
    }
    PlusEmpty <|-- IsEmpty
```

## Реализация в библиотеках

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*

// ... Все операции родителей

summon[IsEmpty[List]].isEmpty(List.empty[Int]) // true
```

---

**Ссылки:**

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/MonadPlus.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/IsEmpty.html)
