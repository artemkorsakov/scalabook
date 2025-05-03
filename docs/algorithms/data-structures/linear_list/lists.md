{%
showPrev = true
prevPage = ../linear_list.html
showNext = true
nextPage = doubly_linked_lists.html
%}

# Связанные списки

_Связанные списки_ реализованы в Scala в виде класса `List`.

Однонаправленные связанные списки используются в тех случаях, когда необходимо эффективно добавлять элемент 
в начало списка, а также разделять список на головной элемент и "хвост" - временная сложность выполнения этих 
операций равна константе. Сложность большинства остальных операций - это _O(N)_, включая вычисление размера списка.

Для того чтобы вычислить размер списка или добавить элемент в конец списка необходимо пройтись по всему списку.
Временная сложность добавления или удаления элемента в заданном индексе может доходить до _O(N)_ в случае индекса,
близкого _N_.

## Определение списка

> Связанный список — это структура данных, состоящая из узлов, 
> где каждый узел содержит данные и ссылку на следующий узел в последовательности. 
> В отличие от массивов, связанные списки не имеют фиксированного размера и могут динамически изменять свою длину.

**Код:**

В следующем определении:

- `LinkedList` — это обобщенный тип, который может содержать элементы любого типа `A`.
- `Nil` представляет пустой список.
- `Cons(head: A, tail: LinkedList[A])` представляет узел списка, где `head` — это значение текущего узла, 
  а `tail` — это ссылка на следующий узел (или пустой список, если это последний узел списка).
- Таким образом, связанный список может быть рекурсивно построен из узлов, каждый из которых указывает на следующий.

```dotty
enum LinkedList[+A]:
  case Nil
  case Cons(head: A, tail: LinkedList[A])

val emptyList: LinkedList[Int] = LinkedList.Nil
// Пустой список
val nonEmptyList: LinkedList[Int] = LinkedList.Cons(3, LinkedList.Cons(2, LinkedList.Cons(1, LinkedList.Nil)))
// Непустой список: (3, 2, 1)
```

## Заглавный элемент

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
enum LinkedList[+A]:
  lazy val headOption: Option[A] = this match
    case Nil        => None
    case Cons(h, _) => Some(h)

emptyList.headOption    // None
nonEmptyList.headOption // Some(3)
```

## Остаток списка без начального элемента

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
enum LinkedList[+A]:
  lazy val tailOption: Option[LinkedList[A]] = this match
    case Nil           => None
    case Cons(_, tail) => Some(tail)

emptyList.tailOption    // None
nonEmptyList.tailOption // Some(Cons(2,Cons(1,Nil)))
```

## Последний элемент

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  lazy val lastOption: Option[A] = this match
    case Nil          => None
    case Cons(h, Nil) => Some(h)
    case Cons(h, t)   => t.lastOption

emptyList.lastOption    // None
nonEmptyList.lastOption // Some(1)
```

## Остаток списка без последнего элемента

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  lazy val initOption: Option[LinkedList[A]] = this match
    case Nil          => None
    case Cons(h, Nil) => Some(Nil)
    case Cons(h, t)   => t.initOption.map(Cons(h, _))

emptyList.initOption    // None
nonEmptyList.initOption // Some(Cons(3,Cons(2,Nil)))
```

## Проверка списка на пустоту

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
enum LinkedList[+A]:
  lazy val isEmpty: Boolean = this match
    case Nil => true
    case _   => false

emptyList.isEmpty       // true
nonEmptyList.isEmpty    // false
```

## Вычисление длины списка

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  lazy val length: Int = this match
    case Nil           => 0
    case Cons(_, tail) => 1 + tail.length

emptyList.length        // 0
nonEmptyList.length     // 3
```

## Добавление в начало списка

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
enum LinkedList[+A]:
  def prepend[B >: A](b: B): LinkedList[B] = Cons(b, this)

emptyList.prepend(5)    // Cons(5,Nil)
nonEmptyList.prepend(5) // Cons(5,Cons(3,Cons(2,Cons(1,Nil))))
```

## Добавление в конец списка

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  def append[B >: A](b: B): LinkedList[B] = this match
    case Nil              => Cons(b, Nil)
    case Cons(head, tail) => Cons(head, tail.append(b))

emptyList.append(5)     // Cons(5,Nil)
nonEmptyList.append(5)  // Cons(3,Cons(2,Cons(1,Cons(5,Nil))))
```

## Получение элемента по заданному индексу

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  def get(i: Int): Option[A] =
    if i < 0 then None
    else if i == 0 then headOption
    else tailOption.flatMap(_.get(i - 1))

emptyList.get(0)        // None
nonEmptyList.get(0)     // Some(3)
```

## Проверяет, содержит ли список заданный элемент

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  def contains[B >: A](x: B): Boolean = this match
    case Nil                        => false
    case Cons(head, _) if head == x => true
    case Cons(_, tail)              => tail.contains(x)  

emptyList.contains(0)    // false
nonEmptyList.contains(2) // true
```

## Объединение двух списков

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  def concat[B >: A](list: LinkedList[B]): LinkedList[B] = this match
    case Nil              => list
    case Cons(head, tail) => Cons(head, tail.concat(list))   

emptyList.concat(nonEmptyList)    // Cons(3,Cons(2,Cons(1,Nil)))
nonEmptyList.concat(nonEmptyList) // Cons(3,Cons(2,Cons(1,Cons(3,Cons(2,Cons(1,Nil))))))
```

## Фильтрация элементов списка

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  def filter(p: A => Boolean): LinkedList[A] = this match
    case Nil                         => Nil
    case Cons(head, tail) if p(head) => Cons(head, tail.filter(p))
    case Cons(_, tail)               => tail.filter(p)  

emptyList.filter(_ % 2 == 0)    // Nil
nonEmptyList.filter(_ % 2 == 0) // Cons(2,Nil)
```

## Преобразование элементов списка

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  def map[B](f: A => B): LinkedList[B] = this match
    case Nil              => Nil
    case Cons(head, tail) => Cons(f(head), tail.map(f))

emptyList.map(_ + 1)            // Nil
nonEmptyList.map(_ + 1)         // Cons(4,Cons(3,Cons(2,Nil)))
```

## Объединение элементов списка

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  def fold[B](init: B)(op: (B, A) => B): B = this match
    case Nil              => init
    case Cons(head, tail) => tail.fold(op(init, head))(op)

emptyList.fold(100)(_ + _)      // 100
nonEmptyList.fold(100)(_ + _)   // 106
```

## Список из первых n элементов

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  def take(n: Int): LinkedList[A] = this match
    case Cons(head, tail) if n > 0 => Cons(head, tail.take(n - 1))
    case _ => Nil

emptyList.take(2)    // Nil 
nonEmptyList.take(2) // Cons(3,Cons(2,Nil))
```

## Список за исключением первых n элементов

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  def drop(n: Int): LinkedList[A] = this match
    case list if n <= 0            => list
    case Cons(head, tail) if n > 0 => tail.drop(n - 1)
    case _                         => Nil

emptyList.drop(1)    // Nil
nonEmptyList.drop(1) // Cons(2,Cons(1,Nil)) 
```

## Список из первых элементов, удовлетворяющих предикату

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  def takeWhile(p: A => Boolean): LinkedList[A] = this match
    case Cons(head, tail) if p(head) => Cons(head, tail.takeWhile(p))
    case _ => Nil

emptyList.takeWhile(_ > 1)    // Nil
nonEmptyList.takeWhile(_ > 1) // Cons(3,Cons(2,Nil))
```

## Список за исключением первых элементов, удовлетворяющих предикату

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  def dropWhile(p: A => Boolean): LinkedList[A] = this match
    case Cons(head, tail) if p(head) => tail.dropWhile(p)
    case _ => this  

emptyList.dropWhile(_ > 1)    // Nil
nonEmptyList.dropWhile(_ > 1) // Cons(1,Nil)
```

---

**Ссылки:**

- [Род Стивенс - Алгоритмы. Теория и практическое применение. Глава 3. Связанные списки](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854)
- [Связанный список - Wikipedia](https://ru.wikipedia.org/wiki/%D0%A1%D0%B2%D1%8F%D0%B7%D0%BD%D1%8B%D0%B9_%D1%81%D0%BF%D0%B8%D1%81%D0%BE%D0%BA)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
- [Introduction to Functional Programming - Bird, Wadler, Chapter 3](https://www.amazon.com/Introduction-functional-programming-Prentice-international/dp/0134841891)
- [Scalacaster - List](https://github.com/vkostyukov/scalacaster/blob/master/src/collection/List.scala)
