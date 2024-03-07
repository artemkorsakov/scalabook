# Куча

[Куча][heap wiki] (_heap_) — это специализированная структура данных типа дерево, которая удовлетворяет свойству кучи:
если B является узлом-потомком узла A, то ключ(A) ≥ ключ(B).
Из этого следует, что элемент с наибольшим ключом всегда является корневым узлом кучи,
поэтому иногда такие кучи называют _max-кучами_
(в качестве альтернативы, если сравнение перевернуть,
то наименьший элемент будет всегда корневым узлом, такие кучи называют _min-кучами_).

## Стандартная двоичная куча

[Двоичная куча, пирамида, или сортирующее дерево][bin heap wiki] — 
такое двоичное дерево, для которого выполнены три условия:

- Значение в любой вершине не меньше (не больше для min-куч), чем значения её потомков
- Глубина всех листьев (расстояние до корня) различается не более чем на 1 слой
- Последний слой заполняется слева направо без "дырок"

**Оценка:**

- _Минимальный элемент кучи_ (`min`)
    - **Время** - O(1)
    - **Память** - O(1)
- _Вставка элемента в кучу_ (`insert`)
    - **Время** - O(log n)
    - **Память** - O(log n)
- _Удаление элемента из кучи_ (`remove`)
  - **Время** - O(log n)
  - **Память** - O(log n)
- _Соединение двух куч_ (`merge`)
    - **Время** - O(n)
    - **Память** - O(n)

**Код:**

```dotty
enum BinaryHeap[+A]:
  import BinaryHeap.*

  private case EmptyHeap

  private case NonEmptyHeap(
      min: A,
      left: BinaryHeap[A],
      right: BinaryHeap[A],
      override val size: Int,
      override val height: Int
  )

  lazy val isEmpty: Boolean = this match
    case EmptyHeap => true
    case _         => false

  val size: Int = this match
    case neh: NonEmptyHeap[A] => neh.size
    case _                    => 0

  val height: Int = this match
    case neh: NonEmptyHeap[A] => neh.height
    case _                    => 0

  lazy val minOption: Option[A] = this match
    case neh: NonEmptyHeap[A] => Some(neh.min)
    case _                    => None

  def insert[B >: A: Ordering](x: B): BinaryHeap[B] = this match
    case EmptyHeap => make(x)
    case NonEmptyHeap(min, left, right, size, height) =>
      if left.size < left.height * left.height - 1 then
        bubbleUp(min, left.insert(x), right)
      else if right.size < right.height * right.height - 1 then
        bubbleUp(min, left, right.insert(x))
      else if right.height < left.height then
        bubbleUp(min, left, right.insert(x))
      else
        bubbleUp(min, left.insert(x), right)

  def remove[B >: A: Ordering]: Option[BinaryHeap[B]] =
    def floatLeft(x: A, l: BinaryHeap[A], r: BinaryHeap[A]): BinaryHeap[A] =
      l match
        case NonEmptyHeap(y, lt, rt, _, _) => make(y, make(x, lt, rt), r)
        case _                             => make(x, l, r)

    def floatRight(x: A, l: BinaryHeap[A], r: BinaryHeap[A]): BinaryHeap[A] =
      r match
        case NonEmptyHeap(y, lt, rt, _, _) => make(y, l, make(x, lt, rt))
        case _                             => make(x, l, r)

    def mergeChildren(l: BinaryHeap[A], r: BinaryHeap[A]): BinaryHeap[A] =
      (l, r) match
        case (EmptyHeap, EmptyHeap) => EmptyHeap
        case (EmptyHeap, NonEmptyHeap(rmin, rleft, rright, rsize, rheight)) =>
          floatRight(rmin, l, mergeChildren(rleft, rright))
        case (NonEmptyHeap(lmin, lleft, lright, lsize, lheight), EmptyHeap) =>
          floatLeft(lmin, mergeChildren(lleft, lright), r)
        case (
              NonEmptyHeap(lmin, lleft, lright, lsize, lheight),
              NonEmptyHeap(rmin, rleft, rright, rsize, rheight)
            ) =>
          if lsize < lheight * lheight - 1 then
            floatLeft(lmin, mergeChildren(lleft, lright), r)
          else if rsize < rheight * rheight - 1 then
            floatRight(rmin, l, mergeChildren(rleft, rright))
          else if rheight < lheight then
            floatLeft(lmin, mergeChildren(lleft, lright), r)
          else floatRight(rmin, l, mergeChildren(rleft, rright))

    def bubbleRootDown(h: BinaryHeap[A]): BinaryHeap[A] = h match
      case NonEmptyHeap(min, left, right, _, _) =>
        bubbleDown(min, left, right)
      case _ => EmptyHeap

    this match
      case NonEmptyHeap(_, left, right, _, _) =>
        Some(bubbleRootDown(mergeChildren(left, right)))
      case _ => None
  end remove
end BinaryHeap

object BinaryHeap:
  def empty[A]: BinaryHeap[A] = EmptyHeap

  def make[A: Ordering](x: A): BinaryHeap[A] =
    make(x, EmptyHeap, EmptyHeap)

  private def make[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] =
    NonEmptyHeap(x, l, r, l.size + r.size + 1, math.max(l.height, r.height) + 1)

  private def bubbleUp[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] = (l, r) match
    case (NonEmptyHeap(y, lt, rt, _, _), _) if x > y =>
      make(y, make(x, lt, rt), r)
    case (_, NonEmptyHeap(z, lt, rt, _, _)) if x > z =>
      make(z, l, make(x, lt, rt))
    case (_, _) => make(x, l, r)

  private def bubbleDown[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] = (l, r) match
    case (NonEmptyHeap(y, _, _, _, _), NonEmptyHeap(z, lt, rt, _, _))
        if z < y && z < x =>
      make(z, l, bubbleDown(x, lt, rt))
    case (NonEmptyHeap(y, lt, rt, _, _), _) if x > y =>
      make(y, bubbleDown(x, lt, rt), r)
    case (_, _) => make(x, l, r)
end BinaryHeap
```

**Пример:**

```dotty
val emptyBinaryHeap: BinaryHeap[Int]    = BinaryHeap.empty
val nonEmptyBinaryHeap: BinaryHeap[Int] = BinaryHeap.empty.insert(1).insert(2)
 
emptyBinaryHeap.isEmpty)       // true
nonEmptyBinaryHeap.isEmpty)    // false

emptyBinaryHeap.size           // 0
nonEmptyBinaryHeap.size        // 2
  
emptyBinaryHeap.height         // 0
nonEmptyBinaryHeap.height      // 2
  
emptyBinaryHeap.minOption      // None
nonEmptyBinaryHeap.minOption   // Some(1)

emptyBinaryHeap.insert(5)    
// NonEmptyHeap(5,EmptyHeap,EmptyHeap,1,1)  
nonEmptyBinaryHeap.insert(5)
// NonEmptyHeap(1,NonEmptyHeap(2,EmptyHeap,EmptyHeap,1,1),NonEmptyHeap(5,EmptyHeap,EmptyHeap,1,1),3,2)

emptyBinaryHeap.remove         // None
nonEmptyBinaryHeap.remove
// Some(NonEmptyHeap(2,EmptyHeap,EmptyHeap,1,1))   
```

## Левосторонняя куча

---

**Ссылки:**

- [Куча - Wiki][heap wiki]
- [Двоичная куча - Wiki][bin heap wiki]
- [Куча - Scalacaster](https://github.com/vkostyukov/scalacaster/blob/master/src/heap)
- [A Functional Approach to Standard Binary Heaps - Vladimir Kostyukov](https://arxiv.org/pdf/1312.4666v1.pdf)

[heap wiki]: https://ru.wikipedia.org/wiki/%D0%9A%D1%83%D1%87%D0%B0_(%D1%81%D1%82%D1%80%D1%83%D0%BA%D1%82%D1%83%D1%80%D0%B0_%D0%B4%D0%B0%D0%BD%D0%BD%D1%8B%D1%85)
[bin heap wiki]: https://ru.wikipedia.org/wiki/%D0%94%D0%B2%D0%BE%D0%B8%D1%87%D0%BD%D0%B0%D1%8F_%D0%BA%D1%83%D1%87%D0%B0
