{%
showPrev = true
prevPage = trees/binary-search-tree.html
showNext = true
nextPage = graph.html
%}

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
- _Объединение двух куч_ (`merge`)
    - **Время** - O(n)
    - **Память** - O(n)

**Код:**

```dotty
enum BinaryHeap[+A]:
  import BinaryHeap.*

  case Leaf

  private case Branch(
      min: A,
      left: BinaryHeap[A],
      right: BinaryHeap[A],
      override val size: Int,
      override val height: Int
  )

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  val size: Int = this match
    case neh: Branch[A] => neh.size
    case _              => 0

  val height: Int = this match
    case neh: Branch[A] => neh.height
    case _              => 0

  lazy val minOption: Option[A] = this match
    case neh: Branch[A] => Some(neh.min)
    case _              => None

  def insert[B >: A: Ordering](x: B): BinaryHeap[B] = this match
    case Leaf => BinaryHeap(x)
    case Branch(min, left, right, size, height) =>
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
        case Branch(y, lt, rt, _, _) => BinaryHeap(y, BinaryHeap(x, lt, rt), r)
        case _                       => BinaryHeap(x, l, r)

    def floatRight(x: A, l: BinaryHeap[A], r: BinaryHeap[A]): BinaryHeap[A] =
      r match
        case Branch(y, lt, rt, _, _) => BinaryHeap(y, l, BinaryHeap(x, lt, rt))
        case _                       => BinaryHeap(x, l, r)

    def mergeChildren(l: BinaryHeap[A], r: BinaryHeap[A]): BinaryHeap[A] =
      (l, r) match
        case (Leaf, Leaf) => Leaf
        case (Leaf, Branch(rmin, rleft, rright, rsize, rheight)) =>
          floatRight(rmin, l, mergeChildren(rleft, rright))
        case (Branch(lmin, lleft, lright, lsize, lheight), Leaf) =>
          floatLeft(lmin, mergeChildren(lleft, lright), r)
        case (
              Branch(lmin, lleft, lright, lsize, lheight),
              Branch(rmin, rleft, rright, rsize, rheight)
            ) =>
          if lsize < lheight * lheight - 1 then
            floatLeft(lmin, mergeChildren(lleft, lright), r)
          else if rsize < rheight * rheight - 1 then
            floatRight(rmin, l, mergeChildren(rleft, rright))
          else if rheight < lheight then
            floatLeft(lmin, mergeChildren(lleft, lright), r)
          else floatRight(rmin, l, mergeChildren(rleft, rright))

    def bubbleRootDown(h: BinaryHeap[A]): BinaryHeap[A] = h match
      case Branch(min, left, right, _, _) =>
        bubbleDown(min, left, right)
      case _ => Leaf

    this match
      case Branch(_, left, right, _, _) =>
        Some(bubbleRootDown(mergeChildren(left, right)))
      case _ => None
  end remove
end BinaryHeap

object BinaryHeap:
  def empty[A]: BinaryHeap[A] = Leaf

  def apply[A: Ordering](x: A): BinaryHeap[A] =
    BinaryHeap(x, Leaf, Leaf)

  private def apply[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] =
    Branch(x, l, r, l.size + r.size + 1, math.max(l.height, r.height) + 1)

  private def bubbleUp[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] = (l, r) match
    case (Branch(y, lt, rt, _, _), _) if x > y =>
      BinaryHeap(y, BinaryHeap(x, lt, rt), r)
    case (_, Branch(z, lt, rt, _, _)) if x > z =>
      BinaryHeap(z, l, BinaryHeap(x, lt, rt))
    case (_, _) => BinaryHeap(x, l, r)

  private def bubbleDown[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] = (l, r) match
    case (Branch(y, _, _, _, _), Branch(z, lt, rt, _, _))
        if z < y && z < x =>
      BinaryHeap(z, l, bubbleDown(x, lt, rt))
    case (Branch(y, lt, rt, _, _), _) if x > y =>
      BinaryHeap(y, bubbleDown(x, lt, rt), r)
    case (_, _) => BinaryHeap(x, l, r)
end BinaryHeap
```

**Пример:**

```dotty
val emptyBinaryHeap: BinaryHeap[Int]    = BinaryHeap.empty
val nonEmptyBinaryHeap: BinaryHeap[Int] = BinaryHeap.empty.insert(1).insert(2)
 
emptyBinaryHeap.isEmpty        // true
nonEmptyBinaryHeap.isEmpty     // false

emptyBinaryHeap.size           // 0
nonEmptyBinaryHeap.size        // 2
  
emptyBinaryHeap.height         // 0
nonEmptyBinaryHeap.height      // 2
  
emptyBinaryHeap.minOption      // None
nonEmptyBinaryHeap.minOption   // Some(1)

emptyBinaryHeap.insert(5)    
// Branch(5,Leaf,Leaf,1,1) 
nonEmptyBinaryHeap.insert(5)
// Branch(1,Branch(2,Leaf,Leaf,1,1),Branch(5,Leaf,Leaf,1,1),3,2)

emptyBinaryHeap.remove         
// None
nonEmptyBinaryHeap.remove
// Some(Branch(2,Leaf,Leaf,1,1))
```

## Левосторонняя куча

Реализация левойсторонней кучи Окасаки, которая удовлетворяет двум свойствам:

- Свойство, упорядоченное по куче: `value(node) <= value(node.left)`, а также `value(node) <= value(node.right)`
- Свойство левосторонности: `rank(node.left) >= rank(node.right)`

, где `rank` — это самый правый элемент кучи (крайний правый путь от корня до пустого узла). 
Объединив эти свойства вместе, можно гарантировать время выполнения не более O(log n) 
для операций вставки/удаления в левосторонней куче.

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
- _Объединение двух куч_ (`merge`)
    - **Время** - O(log n)
    - **Память** - O(log n)

**Код:**

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

**Пример:**

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

## Парная куча (pairing heap)

[Парные кучи][pairing heap wiki] представляют собой многонаправленные древовидные структуры, упорядоченные по куче, 
и их можно считать упрощенными кучами Фибоначчи.

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
- _Объединение двух куч_ (`merge`)
    - **Время** - O(log n)
    - **Память** - O(log n)

**Код:**

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

## Косая куча (skew heap)

[Косая куча][skew heap wiki] - это самонастраивающаяся форма левой кучи, 
которая пытается поддерживать баланс путем безусловной замены всех узлов на пути слияния при слиянии двух куч. 
(Операция слияния также используется при добавлении и удалении значений.)

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
- _Объединение двух куч_ (`merge`)
    - **Время** - O(log n)
    - **Память** - O(log n)

**Код:**

```dotty
enum SkewHeap[+A]:
  import SkewHeap.*

  case Leaf
  private case Branch(min: A, left: SkewHeap[A], right: SkewHeap[A])

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  def insert[B >: A: Ordering](x: B): SkewHeap[B] =
    merge(apply(x), this)

  def remove[B >: A: Ordering]: Option[SkewHeap[B]] = this match
    case Branch(_, left, right) => Some(merge(left, right))
    case _                      => None

end SkewHeap

object SkewHeap:
  def empty[A]: SkewHeap[A] = Leaf

  def apply[A: Ordering](x: A): SkewHeap[A] =
    Branch(x, Leaf, Leaf)

  def merge[A: Ordering](x: SkewHeap[A], y: SkewHeap[A]): SkewHeap[A] =
    (x, y) match
      case (_, Leaf) => x
      case (Leaf, _) => y
      case (Branch(x1, l1, r1), Branch(x2, l2, r2)) =>
        if x1 < x2 then
          Branch(x1, merge(Branch(x2, l2, r2), r1), l1)
        else Branch(x2, merge(Branch(x1, l1, r1), r2), l2)

end SkewHeap
```

---

**Ссылки:**

- [Исходный код][source]
- [Куча - Wiki][heap wiki]
- [Двоичная куча - Wiki][bin heap wiki]
- [Pairing heap - Wiki][pairing heap wiki]
- [Skew heap - Wiki][skew heap wiki]
- [Куча - Scalacaster](https://github.com/vkostyukov/scalacaster/blob/master/src/heap)
- [A Functional Approach to Standard Binary Heaps - Vladimir Kostyukov](http://arxiv.org/pdf/1312.4666v1)

[heap wiki]: https://ru.wikipedia.org/wiki/%D0%9A%D1%83%D1%87%D0%B0_(%D1%81%D1%82%D1%80%D1%83%D0%BA%D1%82%D1%83%D1%80%D0%B0_%D0%B4%D0%B0%D0%BD%D0%BD%D1%8B%D1%85)
[bin heap wiki]: https://ru.wikipedia.org/wiki/%D0%94%D0%B2%D0%BE%D0%B8%D1%87%D0%BD%D0%B0%D1%8F_%D0%BA%D1%83%D1%87%D0%B0
[pairing heap wiki]: https://en.wikipedia.org/wiki/Pairing_heap
[skew heap wiki]: https://en.wikipedia.org/wiki/Skew_heap
[source]: https://gitverse.ru/scalabook/algorithms
