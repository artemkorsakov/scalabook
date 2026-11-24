# Куча

Куча ([далее — heap wiki][heap wiki]) — это специализированная структура данных типа дерево, которая удовлетворяет свойству кучи:
если B является узлом-потомком узла A, то ключ(A) ≥ ключ(B).
Из этого следует, что элемент с наибольшим ключом всегда является корневым узлом кучи,
поэтому иногда такие кучи называют _max-кучами_
(в качестве альтернативы, если сравнение перевернуть,
то наименьший элемент будет всегда корневым узлом, такие кучи называют _min-кучами_).

## Для чего нужны кучи

Кучи решают самую востребованную задачу из области приоритетных очередей: поддерживать множество элементов
с возможностью быстро получить (и/или извлечь) **экстремальный** элемент — максимальный или минимальный.
В отличие от обычной очереди, где порядок определяется временем поступления,
приоритетная очередь всегда отдаёт элемент с наибольшим (или наименьшим) приоритетом.

Такая структура лежит в основе множества алгоритмов и систем:

- **Сортировка** — сортирующее дерево (см. пирамидальную сортировку) позволяет отсортировать массив за O(n log n).
- **Жадные алгоритмы** — например, алгоритм Дейкстры для поиска кратчайших путей и алгоритм Прима для минимального остовного дерева напрямую опираются на мин-кучу.
- **Планировщики задач** — операционные системы и пулы потоков используют очереди приоритетов для выбора следующей задачи.
- **Слияние потоков данных** — слияние k отсортированных массивов в один выполняется за O(n log k).

## Свойство кучи

Формально куча — это бинарное дерево (или в общем случае дерево), для каждой вершины которого выполняется
**инвариант упорядоченности**: ключ вершины не меньше ключей всех её потомков (для max-кучи) либо не больше (для min-кучи).
Благодаря трансзитивности отношения порядка этот инвариант гарантирует, что наибольший (наименьший) ключ
всегда находится в корне. Поэтому операция получения экстремума выполняется за O(1).

Важно понимать, что свойство кучи **не** задаёт полный порядок элементов: деревья с одним и тем же множеством
ключей могут иметь разную форму. Это отличие от деревьев поиска, где упорядоченность жёстко регламентирует
относительное расположение ключей и позволяет искать произвольный элемент. Куча не умеет быстро искать
произвольный элемент по значению — её сила именно в быстром доступе к экстремуму.

## Стандартная двоичная куча

[Двоичная куча, пирамида, или сортирующее дерево][bin heap wiki] — 
такое двоичное дерево, для которого выполнены три условия:

- **Упорядоченность по куче.** Значение в любой вершине не меньше (не больше для min-куч), чем значения её потомков.
  Это базовый инвариант, описанный выше.
- **Почти полная заполненность.** Глубина всех листьев (расстояние до корня) различается не более чем на 1 слой,
  то есть дерево всегда сбалансировано и имеет высоту O(log n). Именно балансировка даёт логарифмическую
  гарантию для вставки и удаления.
- **Последовательность заполнения.** Последний слой заполняется слева направо без "дырок".
  Последние два свойства вместе означают, что дерево представимо плотным массивом без промежутков,
  где индексы детей вершины `i` равны `2i + 1` и `2i + 2`.

### Как работают вставка и удаление

**Функциональная** реализация на Scala использует неизменяемые (`immutable`) структуры данных. 
Каждая операция возвращает новую кучу, не модифицируя исходную.
Благодаря структурному разделению (sharing) узлы, не затрагиваемые операцией, переиспользуются между старой
и новой версией дерева, что сохраняет эффективность даже в чисто функциональном стиле.

Приведённая реализация дополнительно поддерживает поля `size` и `height`, что позволяет реализовать
проверку "почти полной" заполненности без видимых дырок и корректно направлять вставку в то из поддеревьев,
которое ещё не полностью заполнено.

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
    - **Время** - O(n)
    - **Память** - O(n)

### Код

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

### Разбор реализации на Scala

Взглянем на ключевые моменты кода:

- **Рекурсивная структура.** Куча описывается алгебраическим типом данных: `Leaf` — пустая куча,
  `Branch(min, left, right, size, height)` — непустой узел, где `min` хранит минимум всего поддерева.
  Поля `size` и `height` кэшируются в узле, поэтому вычисляются за O(1) без повторного обхода.
- **Вставка.** Метод ищет поддерево, которое ещё не полностью заполнено (`left.size < left.height * left.height - 1`),
  и рекурсивно помещает элемент туда, после чего вызовом `bubbleUp` восстанавливает инвариант.
  Условие с квадратом высоты проверяет, достиг ли узел максимально возможного размера для своей высоты:
  у полного дерева высоты `h` все уровни заполнены, поэтому его размер близок к `(h + 1)² - 1`.
- **Удаление.** Удаляется корень; затем его дети объединяются через `mergeChildren`, после чего корень
  "проталкивается" вниз через `bubbleDown`, чтобы инвариант кучи сохранился.
- **Просеивание.** `bubbleUp` и `bubbleDown` — это классические операции восстановления порядка:
  первая поднимает нарушивший порядок элемент к корню, вторая опускает его вниз, меняя местами
  с бо́льшим из детей.

### Пример

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

Пример наглядно демонстрирует базовое поведение: пустая куча сообщает `isEmpty = true`, `minOption = None`,
а удаление из неё возвращает `None`. После вставки элементов корень всегда хранит минимальное значение,
а повторное удаление корня восстанавливает инвариант, оставляя в корне следующее по величине значение.

## Разновидности куч

Простая двоичная куча прекрасно справляется с вставкой и извлечением экстремума за O(log n),
однако её операция объединения двух куч (`merge`) стоит целых O(n). Во многих алгоритмах важно
уметь эффективно сливать две очереди приоритетов. Для этого придуманы специальные разновидности куч:

- **[Левосторонняя куча](heap/leftist_heap.md)** — восстанавливает баланс за счёт жёсткого инварианта на ранг,
  гарантируя O(log n) на слияние. Описана в одноимённом разделе.
- **[Косая куча (skew heap)](heap/skew_heap.md)** — самонастраивающаяся вариация левосторонней кучи,
  которая при каждом слиянии безусловно меняет местами детей. Даёт амортизированное O(log n).
- **[Парная куча (pairing heap)](heap/pairing_heap.md)** — многонаправленное дерево, упрощённая куча Фибоначчи.
  На практике показывает отличную производительность при простой реализации.

## Итоги

Куча — одна из фундаментальных структур данных для работы с приоритетными очередями.
Она даёт O(1) на получение экстремума и O(log n) на вставку и удаление, при этом реализуется
буквально за несколько десятков строк. Выбор конкретной разновидности кучи сводится к тому,
какие операции критичны для задачи: если нужно часто сливать кучи — выбирайте левостороннюю,
косую или парную кучу.

---

**Ссылки:**

- [Исходный код][source]
- [Куча - Wiki][heap wiki]
- [Двоичная куча - Wiki][bin heap wiki]
- [Левосторонняя куча][leftist heap]
- [Pairing heap - Wiki][pairing heap wiki]
- [Skew heap - Wiki][skew heap wiki]
- [Алгоритмика - Куча](https://ru.algorithmica.org/cs/basic-structures/heap/)
- [Куча - Scalacaster](https://github.com/vkostyukov/scalacaster/blob/master/src/heap)
- [A Functional Approach to Standard Binary Heaps - Vladimir Kostyukov](http://arxiv.org/pdf/1312.4666v1)

[heap wiki]: https://ru.wikipedia.org/wiki/%D0%9A%D1%83%D1%87%D0%B0_(%D1%81%D1%82%D1%80%D1%83%D0%BA%D1%82%D1%83%D1%80%D0%B0_%D0%B4%D0%B0%D0%BD%D0%BD%D1%8B%D1%85)
[bin heap wiki]: https://ru.wikipedia.org/wiki/%D0%94%D0%B2%D0%BE%D0%B8%D1%87%D0%BD%D0%B0%D1%8F_%D0%BA%D1%83%D1%87%D0%B0
[leftist heap]: heap/leftist_heap.md
[pairing heap wiki]: https://en.wikipedia.org/wiki/Pairing_heap
[skew heap wiki]: https://en.wikipedia.org/wiki/Skew_heap
[source]: https://gitverse.ru/scalabook/algorithms