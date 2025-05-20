{%
showPrev = true
prevPage = lists.html
showNext = true
nextPage = stacks.html
%}

# Дважды связанные списки

_Дважды связанные списки_ содержат ссылки не только на следующий список, но и на предыдущий.

В двунаправленных связанных списках есть указатели на начало и конец списка - в этом случае время добавления элемента
в конец списка - константа.

Дважды связанный список можно реализовать например таким образом:

```dotty
enum DoublyList[+A]:
  case Nil
  case DoublyListCons(left: DoublyList[A], value: A, right: DoublyList[A])

  lazy val length: Int = this match
    case Nil                            => 0
    case DoublyListCons(left, _, right) => left.length + 1 + right.length

object DoublyList:
  def prepend[A](list: DoublyList[A], a: A): DoublyList[A] = list match
    case Nil => DoublyListCons(Nil, a, Nil)
    case DoublyListCons(left, value, right) =>
      DoublyListCons(prepend(left, a), value, right)

  def append[A](list: DoublyList[A], a: A): DoublyList[A] = list match
    case Nil => DoublyListCons(Nil, a, Nil)
    case DoublyListCons(left, value, right) =>
      DoublyListCons(left, value, append(right, a))

val list = DoublyListCons(DoublyListCons(Nil, 3, Nil), 2, DoublyListCons(Nil, 1, Nil))

list.length                 
// 3
DoublyList.prepend(list, 0)
// DoublyListCons(DoublyListCons(DoublyListCons(Nil,0,Nil),3,Nil),2,DoublyListCons(Nil,1,Nil))
DoublyList.append(list, 4)  
// DoublyListCons(DoublyListCons(Nil,3,Nil),2,DoublyListCons(Nil,1,DoublyListCons(Nil,4,Nil)))
```

---

**Ссылки:**

- [Donald E. Knuth - The Art of Computer Programming, section 2.2.5](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
