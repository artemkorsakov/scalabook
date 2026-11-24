# Косая куча (skew heap)

[Косая куча][skew heap wiki] — это самонастраивающаяся форма левой кучи, 
которая пытается поддерживать баланс путем безусловной замены всех узлов на пути слияния при слиянии двух куч. 
(Операция слияния также используется при добавлении и удалении значений.)

## Идея самонастройки

В левосторонней куче после слияния мы проверяем ранги детей и, если инвариант нарушен,
меняем их местами (функция `swap`). Косая куча идёт дальше: она делает это **безусловно**.
При каждом слиянии мы всегда рекурсивно сливаем правое поддерево корня с другим деревом,
а затем просто меняем левого и правого ребёнка местами, не глядя ни на какие метрики баланса.

За счёт такого "всегда меняем местами" косая куча постепенно адаптирует свою форму
к последовательности операций. Хотя отдельная операция в худшем случае может оказаться
дорогой (вплоть до O(n)), суммарная стоимость последовательности операций амортизированно
составляет O(log n) на операцию. Это классический пример **амортизационного анализа**
и саморегулирующейся структуры данных.

## Сравнение с левосторонней кучей

- **Левосторонняя куча** использует ранг и восстанавливает баланс только при необходимости,
  гарантируя O(log n) в худшем случае.
- **Косая куча** отказывается от рангов вовсе, что делает реализацию ещё проще
  (нет ни `rank`, ни `swap` с проверкой условия). Вместо гарантии худшего случая
  она опирается на амортизированную сложность.

## Операции

Как и у других куч, публичные операции `insert` и `remove` выражаются через `merge`:

- **Вставка** — слияние с одноэлементной кучей.
- **Удаление экстремума** — извлечение корня и слияние его левого и правого поддеревьев.
- **Минимум** — чтение корня, O(1).

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

### Разбор реализации на Scala

- **Без рангов.** В каждом узле хранится только значение и два ребёнка — никаких служебных полей для балансировки.
  Это самая компактная структура среди рассмотренных куч.
- **Merge с поворотом.** Рекурсивно сливается правое поддерево меньшего корня с другим деревом,
  а результат и левое поддерево становятся детьми: правый ребёнок получает результат слияния,
  левый ребёнок — прежнее левое поддерево. Благодаря такому повороту дерево "перекашивается"
  на сторону, противоположную последним операциям, обеспечивая саморегулировку.
- **Минимальный код.** Поскольку `insert` и `remove` сводятся к `merge`, вся логика
  умещается в одно рекурсивное сопоставление с образцом.

### Пример

```dotty
val emptySkewHeap: SkewHeap[Int]     = SkewHeap.empty
val nonEmptySkewHeap: SkewHeap[Int]  = SkewHeap.empty.insert(1).insert(2)

emptySkewHeap.isEmpty       // true
nonEmptySkewHeap.isEmpty    // false

emptySkewHeap.minOption     // None
nonEmptySkewHeap.minOption  // Some(1)

nonEmptySkewHeap.remove
// Some(Branch(2,Leaf,Leaf))
```

Пример подтверждает, что корень всегда минимален, а после удаления корня новая куча снова
устроена корректно.

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