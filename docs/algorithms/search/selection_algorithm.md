# Алгоритм выбора

**Алгоритм:**

[Линейный (в среднем) алгоритм для нахождения k-й порядковой статистики](https://ru.wikipedia.org/wiki/%D0%90%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC_%D0%B2%D1%8B%D0%B1%D0%BE%D1%80%D0%B0)

Алгоритм для нахождения k-й порядковой статистики, основанный на алгоритме быстрой сортировки и работающий за O(n) в среднем.

Идея алгоритма заключается в том, что массив разбивается на две части относительно случайно (равновероятно) 
выбранного элемента — в одну часть попадают элементы, меньшие, чем выбранный, 
в другую — остальные (эта операция выполняется за O(n)), 
по её окончании выбранный элемент находится в позиции `j`. 
Если в первой части оказалось ровно `k-1` элементов (`j = k`), то выбранный элемент является искомым, 
если `j > k`, то алгоритм выполняется рекурсивно для первой части массива, 
иначе — для второй (в последнем случае для следующей итерации от `k` отнимается `j`).

**Оценка:**

- **Время**:
    - Худшее - \\(O(n^{2})\\)
    - Лучшее - \\(O(1)\\)
    - Среднее - \\(O(n)\\)
- **Память** - \\(O(1)\\)

**Код:**

```dotty
def selectionSearch[A: Ordering](list: List[A], n: Int): Option[A] =
  @tailrec
  def partitionAndSearch(as: List[A], m: Int): Option[A] = as match
    case Nil => None
    case h :: t =>
      val (l, p, g) = partition(h, t)
      val len       = l.length
      if len == m - 1 then Some(p)
      else if len >= m then partitionAndSearch(l, m)
      else partitionAndSearch(g, m - len - 1)

  def partition(head: A, tail: List[A]): (List[A], A, List[A]) =
    @tailrec
    def loop(
        rest: List[A],
        less: List[A],
        p: A,
        more: List[A]
    ): (List[A], A, List[A]) =
      rest match
        case Nil             => (less, p, more)
        case h :: t if h < p => loop(t, h :: less, p, more)
        case h :: t          => loop(t, less, p, h :: more)

    loop(tail, Nil, head, Nil)

  if n <= 0 then None
  else partitionAndSearch(list, n)
end selectionSearch

selectionSearch((1 to 100).toList, 45)
// Some(45)
```

---

**Ссылки:**

- [Исходный код][sample]
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Scalacaster - Search algorithms](https://github.com/vkostyukov/scalacaster/tree/master/src/search)

[sample]: https://gitverse.ru/scalabook/algorithms
