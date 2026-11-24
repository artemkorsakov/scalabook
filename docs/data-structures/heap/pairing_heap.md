# Парная куча (pairing heap)

[Парные кучи][pairing heap wiki] (pairing heaps) — это многонаправленные (multi-way) древовидные структуры, упорядоченные по куче, 
и их можно считать упрощенными кучами Фибоначчи.
Они были предложены Фредманом, Седжвиком, Слеатором и Тарьяном в 1986 году
и привлекли внимание необычно простой реализацией при отличных асимптотических и практических свойствах.

## Чем парная куча отличается от двоичной

- **Многонаправленность.** Узлы могут иметь произвольное число детей, хранящихся в виде списка поддеревьев.
  Это принципиально отличает парную кучу от бинарных вариантов (двоичная, левосторонняя, косая), у которых ровно два ребёнка.
- **Порядок по куче.** Ключ любого узла не превосходит ключи всех его детей —
  корень всегда содержит минимальный элемент.
- **Простая структура.** Достаточно хранить в каждом узле значение и список подкуч.
  Никаких рангов, высот или дополнительных полей для балансировки не требуется.

## Идея операции pairing

Секрет эффективности парной кучи — в обработке поддеревьев после удаления корня.
Метод `pairing` попарно объединяет поддеревья: сначала сливаются первая и вторая кучи из списка,
затем третья и четвёртая, и так далее, после чего результат сливается в одну кучу.
Эта двухфазная схема ("сначала попарно, затем наращиваем") и даёт амортизированно логарифмическую
сложность операций `insert` и `remove` без сложной логики, характерной для кучи Фибоначчи.

Хотя строгое доказательство амортизированных границ парных куч оказалось нетривиальным
и долгое время оставалось открытым вопросом, на практике парные кучи показывают отличную
производительность и часто оказываются самыми быстрыми среди реализуемых простым способом.

## Операции

Все операции выражаются через вспомогательное слияние двух куч `merge`:

- **Вставка (`insert`)** — слияние кучи из одного элемента с текущей кучей.
- **Удаление экстремума (`remove`)** — извлечение корня и применение `pairing` к списку его поддеревьев.
- **Минимум (`min`)** — просто чтение корня, O(1).

### Оценка

- _Минимальный элемент кучи_ (`min`)
    - **Время** - O(1)
    - **Память** - O(1)
- _Вставка элемента в кучу_ (`insert`)
    - **Время** - O(log n)
    - **Память** - O(log n)
- _Удаление элемента из кучи_ (`remove`)
    - **Время** - O(log n)
    - **Память** - O(log n)
- _Объединение двух куч_ (`merge`)
    - **Время** - O(log n)
    - **Память** - O(log n)

### Код

```dotty
enum PairingHeap[+A]:

  import PairingHeap.*

  case Leaf
  private case Branch(min: A, subtrees: List[PairingHeap[A]])

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  def insert[B >: A: Ordering](x: B): PairingHeap[B] =
    merge(PairingHeap(x), this)

  def remove[B >: A: Ordering]: Option[PairingHeap[B]] = this match
    case Branch(_, subtrees) => Some(pairing(subtrees))
    case _                   => None

end PairingHeap

object PairingHeap:
  def empty[A]: PairingHeap[A] = Leaf

  def apply[A: Ordering](x: A): PairingHeap[A] =
    Branch(x, List.empty)

  private def merge[A: Ordering](
      x: PairingHeap[A],
      y: PairingHeap[A]
  ): PairingHeap[A] =
    (x, y) match
      case (_, Leaf) => x
      case (Leaf, _) => y
      case (Branch(x1, subs1), Branch(x2, subs2)) =>
        if x1 < x2 then
          Branch(x1, Branch(x2, subs2) :: subs1)
        else
          Branch(x2, Branch(x1, subs1) :: subs2)

  @tailrec
  private def pairing[A: Ordering](subtrees: List[PairingHeap[A]])
      : PairingHeap[A] =
    subtrees match
      case Nil              => Leaf
      case hd :: Nil        => hd
      case h1 :: h2 :: tail => pairing(merge(h1, h2) :: tail)

end PairingHeap
```

### Разбор реализации на Scala

- **Список подкуч.** Каждый узел `Branch(min, subtrees)` хранит значение и список дочерних куч.
  Благодаря `List` добавление ребёнка при слиянии выполняется за O(1) добавлением в голову списка.
- **Merge.** Из двух корней выбирается меньший; корень победителя остаётся наверху,
  а проигравшая куча добавляется в его список подкуч. Сложность — O(1).
- **Pairing.** Рекурсивная хвостовая функция обрабатывает список: пустой — пустая куча,
  один элемент — готовый результат, а для двух или более первых куч пара сливается, после чего
  результат подставляется в начало списка и процесс повторяется. Атрибут `@tailrec` гарантирует,
  что рекурсия оптимизируется в цикл и не переполнит стек даже на больших списках.

---

**Ссылки:**

- [Исходный код][source]
- [Куча - Wiki][heap wiki]
- [Двоичная куча - Wiki][bin heap wiki]
- [К левосторонней куче в разделе "Куча"][heap]
- [Pairing heap - Wiki][pairing heap wiki]
- [Skew heap - Wiki][skew heap wiki]
- [Алгоритмика - Куча](https://ru.algorithmica.org/cs/basic-structures/heap/)
- [Куча - Scalacaster](https://github.com/vkostyukov/scalacaster/blob/master/src/heap)
- [A Functional Approach to Standard Binary Heaps - Vladimir Kostyukov](http://arxiv.org/pdf/1312.4666v1)

[heap wiki]: https://ru.wikipedia.org/wiki/%D0%9A%D1%83%D1%87%D0%B0_(%D1%81%D1%82%D1%80%D1%83%D0%BA%D1%82%D1%83%D1%80%D0%B0_%D0%B4%D0%B0%D0%BD%D0%BD%D1%8B%D1%85)
[bin heap wiki]: https://ru.wikipedia.org/wiki/%D0%94%D0%B2%D0%BE%D0%B8%D1%87%D0%BD%D0%B0%D1%8F_%D0%BA%D1%83%D1%87%D0%B0
[heap]: ../heap.md
[pairing heap wiki]: https://en.wikipedia.org/wiki/Pairing_heap
[skew heap wiki]: https://en.wikipedia.org/wiki/Skew_heap
[source]: https://gitverse.ru/scalabook/algorithms