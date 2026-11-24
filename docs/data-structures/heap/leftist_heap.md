# Левосторонняя куча

Левосторонняя куча (leftist heap) — это вариант кучи, предложенный Кларком Крейном (Clark Crane) и впоследствии детально изученный Крисом Окасаки (Chris Okasaki) в его книге
"Purely Functional Data Structures". Она специально спроектирована так, чтобы **объединение двух куч** выполнялось быстро — за O(log n), в отличие от O(n) у обычной двоичной кучи.

## Проблема, которую решает левосторонняя куча

Вспомним, что двоичная куча не умеет быстро сливать две кучи в одну. Однако во многих задачах
(например, при вычислении медианы потока, в алгоритмах на графах или при слиянии приоритетных очередей)
операция `merge` критически важна. Левосторонняя куча жертвует жёсткой балансировкой по глубине,
но получает взамен эффективное объединение.

Ключевая идея состоит в том, чтобы поддерживать структуру дерева **перекошенной влево**:
короткая ветка всегда находится справа. При слиянии мы всегда рекурсивно сливаем правые ветки,
а левая ветка остаётся нетронутой — поэтому объём работы пропорционален длине правого пути,
который мы и ограничиваем снизу.

## Два свойства

Реализация левосторонней кучи Окасаки удовлетворяет двум свойствам:

- **Свойство, упорядоченное по куче:** `value(node) <= value(node.left)`, а также `value(node) <= value(node.right)`.
  Это стандартный инвариант мин-кучи: ключ любой вершины не превосходит ключи её детей.
- **Свойство левосторонности:** `rank(node.left) >= rank(node.right)`.

, где `rank` — это длина самого короткого пути от узла до пустого узла (самый правый элемент кучи —
крайний правый путь от корня до пустого узла).
Формально `rank(Leaf) = 0`, а `rank(Branch(_, l, r, _)) = rank(r) + 1`,
поскольку из-за свойства левосторонности правый путь всегда кратчайший.

Объединив эти свойства вместе, можно гарантировать время выполнения не более O(log n)
для операций вставки/удаления в левосторонней куче.
Почему это работает: левосторонний инвариант гарантирует, что правый путь от любого узла к листу
короткий (не длиннее O(log n)), а слияние и, как следствие, вставка и удаление проходят именно по правым путям.

## Связь с insert и remove

Хотя левосторонняя куча хранит только операции `insert` и `remove` публично, обе легко выражаются через `merge`:

- **Вставка** — это слияние текущей кучи с кучей из одного элемента.
- **Удаление экстремума** — это извлечение корня и слияние его левого и правого поддеревьев.

Таким образом, вся нетривиальная логика сосредоточена в одной функции `merge`, что делает
реализацию компактной и удобной для понимания. После каждого слияния вспомогательная функция `swap`
восстанавливает левосторонний инвариант, при необходимости переставляя детей местами.

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
enum LeftistHeap[+A]:
  import LeftistHeap.*

  case Leaf

  private case Branch(
      min: A,
      left: LeftistHeap[A],
      right: LeftistHeap[A],
      override val rank: Int
  )

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  val rank: Int = this match
    case neh: Branch[A] => neh.rank
    case _              => 0

  lazy val minOption: Option[A] = this match
    case neh: Branch[A] => Some(neh.min)
    case _              => None

  def insert[B >: A: Ordering](x: B): LeftistHeap[B] = this match
    case Branch(min, left, right, _) =>
      if left.rank > right.rank then bubble(min, left, right.insert(x))
      else bubble(min, left.insert(x), right)
    case _ => LeftistHeap(x)

  def remove[B >: A: Ordering]: Option[LeftistHeap[B]] = this match
    case Branch(_, left, right, _) => Some(merge(left, right))
    case _                         => None
end LeftistHeap

object LeftistHeap:
  def empty[A]: LeftistHeap[A] = Leaf

  def apply[A: Ordering](x: A): LeftistHeap[A] =
    LeftistHeap(x, Leaf, Leaf)

  private def apply[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    Branch(x, l, r, r.rank + 1)

  private def bubble[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    (l, r) match
      case (Branch(y, lt, rt, _), _) if x > y =>
        LeftistHeap(y, LeftistHeap(x, lt, rt), r)
      case (_, Branch(z, lt, rt, _)) if x > z =>
        LeftistHeap(z, l, LeftistHeap(x, lt, rt))
      case (_, _) => LeftistHeap(x, l, r)

  def merge[A: Ordering](x: LeftistHeap[A], y: LeftistHeap[A]): LeftistHeap[A] =
    (x, y) match
      case (_, Leaf) => x
      case (Leaf, _) => y
      case (
            Branch(xx, xl, xr, _),
            Branch(yy, yl, yr, _)
          ) =>
        if xx < yy then swap(xx, xl, merge(xr, y))
        else swap(yy, yl, merge(yr, x))

  private def swap[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    if l.rank < r.rank then LeftistHeap(x, r, l)
    else LeftistHeap(x, l, r)

end LeftistHeap
```

### Разбор реализации на Scala

- **Ранг.** В отличие от двоичной кучи, узел хранит не размер и высоту, а именно `rank` —
  длину кратчайшего (правого) пути до пустого узла. При конструировании узла `apply(x, l, r)`
  ранг вычисляется как `r.rank + 1`, поскольку левый ребёнок всегда не короче правого.
- **Merge.** Функция выбирает корень с меньшим ключом и рекурсивно сливает его правое поддерево
  с другой кучей. Затем `swap` проверяет левосторонний инвариант и при нарушении меняет детей местами.
- **Вставка.** Если куча непустая, элемент вставляется в то поддерево, куда его можно добавить,
  сохраняя левостороннюю форму, а `bubble` восстанавливает порядок по куче.
- **Функциональность.** Как и двоичная куча, реализация полностью неизменяема и разделяет
  неизменные поддеревья между версиями за счёт структурного обмена.

### Пример

```dotty
val emptyLeftistHeap: LeftistHeap[Int] = LeftistHeap.empty
val nonEmptyLeftistHeap: LeftistHeap[Int] = LeftistHeap.empty.insert(1).insert(2)
 
emptyLeftistHeap.isEmpty       // true
nonEmptyLeftistHeap.isEmpty    // false
  
emptyLeftistHeap.rank          // 0
nonEmptyLeftistHeap.rank       // 1
  
emptyLeftistHeap.minOption     // None
nonEmptyLeftistHeap.minOption  // Some(1)
  
emptyLeftistHeap.insert(5)
// Branch(5,Leaf,Leaf,1)
nonEmptyLeftistHeap.insert(5)
// Branch(1,Branch(2,Leaf,Leaf,1),Branch(5,Leaf,Leaf,1),2)

emptyLeftistHeap.remove
// None
nonEmptyLeftistHeap.remove
// Some(Branch(2,Leaf,Leaf,1))

LeftistHeap.merge(emptyLeftistHeap, emptyLeftistHeap)
// Leaf
LeftistHeap.merge(emptyLeftistHeap, nonEmptyLeftistHeap)
// Branch(1,Branch(2,Leaf,Leaf,1),Leaf,1)
LeftistHeap.merge(nonEmptyLeftistHeap, nonEmptyLeftistHeap)
// Branch(1,Branch(2,Leaf,Leaf,1),Branch(1,Branch(2,Leaf,Leaf,1),Leaf,1),2)
```

Пример показывает, что корень всегда остаётся минимальным элементом, а операция `merge`
корректно объединяет две кучи в новую левостороннюю кучу, поддерживая оба инварианта.

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