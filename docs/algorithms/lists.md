# Связанные списки

Связанные списки реализованы в Scala в виде класса [`List`](https://scalabook.gitflic.space/docs/scala/collections/list).

Однонаправленные связанные списки используются в тех случаях, когда необходимо эффективно добавлять элемент 
в начало списка, а также разделять список на головной элемент и "хвост" - временная сложность выполнения этих 
операций равна константе. Сложность большинства остальных операций - это _O(N)_, включая вычисление размера списка.

Однонаправленный связанный список можно реализовать например таким образом:

```scala
enum List[+A]:
  case Nil
  case Cons(head: A, tail: List[A])

  lazy val length: Int = this match
    case Nil           => 0
    case Cons(_, tail) => 1 + tail.length

object List:
  def prepend[A](list: List[A], a: A): List[A] =
    Cons(a, list)

  def append[A](list: List[A], a: A): List[A] = list match
    case Nil              => Cons(a, Nil)
    case Cons(head, tail) => Cons(head, append(tail, a))
```

Для того чтобы вычислить размер списка или добавить элемент в конец списка необходимо пройтись по всему списку.
Временная сложность добавления или удаления элемента в заданном индексе может доходить до _O(N)_ в случае индекса,
близкого _N_.

В двунаправленных связанных списках есть указатели на начало и конец списка - в этом случае время добавления элемента
в конец списка - константа.


---

**Ссылки:**

- [Род Стивенс - Алгоритмы. Теория и практическое применение. Глава 3. Связанные списки](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
