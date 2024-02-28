# Связанные списки

_Связанные списки_ реализованы в Scala в виде класса [`List`](/scala/collections/list.md).

Однонаправленные связанные списки используются в тех случаях, когда необходимо эффективно добавлять элемент 
в начало списка, а также разделять список на головной элемент и "хвост" - временная сложность выполнения этих 
операций равна константе. Сложность большинства остальных операций - это _O(N)_, включая вычисление размера списка.

Для того чтобы вычислить размер списка или добавить элемент в конец списка необходимо пройтись по всему списку.
Временная сложность добавления или удаления элемента в заданном индексе может доходить до _O(N)_ в случае индекса,
близкого _N_.

**Оценка:**

- _Заглавный элемент_ (`headOption`) 
    - **Время** - O(1)
    - **Память** - O(1)
- _Остаток списка без начального элемента_ (`tailOption`)
    - **Время** - O(1)
    - **Память** - O(1)
- _Проверка списка на пустоту_ (`isEmpty`)
    - **Время** - O(1)
    - **Память** - O(1)
- _Вычисление длины списка_ (`length`)
    - **Время** - O(n)
    - **Память** - O(n)
- _Добавление в начало_ (`prepend`)
    - **Время** - O(1)
    - **Память** - O(1)
- _Добавление в конец списка_ (`append`)
    - **Время** - O(n)
    - **Память** - O(n)
- _Получение элемента по заданному индексу_ (`get`)
    - **Время** - O(n)
    - **Память** - O(n)
- _Проверяет, содержит ли список заданный элемент_ (`contains`)
    - **Время** - O(n)
    - **Память** - O(n)
- _Объединение двух списков_ (`concat`)
    - **Время** - O(n)
    - **Память** - O(n)
- _Фильтрация элементов списка_ (`filter`)
    - **Время** - O(n)
    - **Память** - O(n)
- _Преобразование элементов списка_ (`map`)
    - **Время** - O(n)
    - **Память** - O(n)
- _Объединение элементов списка_ (`fold`)
    - **Время** - O(n)
    - **Память** - O(n)

**Код:**

```dotty
enum LinkedList[+A]:
  case Nil
  case Cons(head: A, tail: LinkedList[A])

  lazy val headOption: Option[A] = this match
    case Nil        => None
    case Cons(h, _) => Some(h)

  lazy val tailOption: Option[LinkedList[A]] = this match
    case Nil           => None
    case Cons(_, tail) => Some(tail)

  lazy val isEmpty: Boolean = this match
    case Nil => true
    case _   => false

  lazy val length: Int = this match
    case Nil           => 0
    case Cons(_, tail) => 1 + tail.length

  def prepend[B >: A](b: B): LinkedList[B] = Cons(b, this)

  def append[B >: A](b: B): LinkedList[B] = this match
    case Nil              => Cons(b, Nil)
    case Cons(head, tail) => Cons(head, tail.append(b))
    
  def get(i: Int): Option[A] =
    if i < 0 then None
    else if i == 0 then headOption
    else tailOption.flatMap(_.get(i - 1))
    
  def contains[B >: A](x: B): Boolean = this match
    case Nil                        => false
    case Cons(head, _) if head == x => true
    case Cons(_, tail)              => tail.contains(x)    
    
  def concat[B >: A](list: LinkedList[B]): LinkedList[B] = this match
    case Nil              => list
    case Cons(head, tail) => Cons(head, tail.concat(list))  
    
  def filter(p: A => Boolean): LinkedList[A] = this match
    case Nil                         => Nil
    case Cons(head, tail) if p(head) => Cons(head, tail.filter(p))
    case Cons(_, tail)               => tail.filter(p)

  def map[B](f: A => B): LinkedList[B] = this match
    case Nil              => Nil
    case Cons(head, tail) => Cons(f(head), tail.map(f))

  def fold[B](init: B)(op: (B, A) => B): B = this match
    case Nil              => init
    case Cons(head, tail) => tail.fold(op(init, head))(op)    
end LinkedList
```

**Пример:**

```dotty
val emptyList: LinkedList[Int] = LinkedList.Nil
val nonEmptyList: LinkedList[Int] = LinkedList.Cons(2, LinkedList.Cons(1, LinkedList.Nil))

emptyList.headOption    // None
nonEmptyList.headOption // Some(2)

emptyList.tailOption    // None
nonEmptyList.tailOption // Some(Cons(1,Nil))

emptyList.isEmpty       // true
nonEmptyList.isEmpty    // false

emptyList.length        // 0
nonEmptyList.length     // 2

emptyList.prepend(5)    // Cons(5,Nil)
nonEmptyList.prepend(5) // Cons(5,Cons(2,Cons(1,Nil)))

emptyList.append(5)     // Cons(5,Nil)
nonEmptyList.append(5)  // Cons(2,Cons(1,Cons(5,Nil)))

emptyList.get(0)        // None
nonEmptyList.get(0)     // Some(2)
  
emptyList.contains(0)    // false
nonEmptyList.contains(2) // true
  
emptyList.concat(nonEmptyList)    // Cons(2,Cons(1,Nil))
nonEmptyList.concat(nonEmptyList) // Cons(2,Cons(1,Cons(2,Cons(1,Nil))))

emptyList.filter(_ % 2 == 0)    // Nil
nonEmptyList.filter(_ % 2 == 0) // Cons(2,Nil)

emptyList.map(_ + 1)            // Nil
nonEmptyList.map(_ + 1)         // Cons(3,Cons(2,Nil))

emptyList.fold(100)(_ + _)      // 100
nonEmptyList.fold(100)(_ + _)   // 103
```

---

**Ссылки:**

- [Род Стивенс - Алгоритмы. Теория и практическое применение. Глава 3. Связанные списки](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854)
- [Связанный список - Wikipedia](https://ru.wikipedia.org/wiki/%D0%A1%D0%B2%D1%8F%D0%B7%D0%BD%D1%8B%D0%B9_%D1%81%D0%BF%D0%B8%D1%81%D0%BE%D0%BA)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
- [Scalacaster - List](https://github.com/vkostyukov/scalacaster/blob/master/src/collection/List.scala)
