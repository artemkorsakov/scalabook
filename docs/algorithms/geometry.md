# Геометрия

## Расстояние между точками

**Алгоритм:**

Определим абстрактный тип данных `Point` и две его реализации: `CartesianPoint` и `PolarPoint`. 
`CartesianPoint` представляет точку в декартовой системе координат, состоящей из двух координат `x` и `y`. 
`PolarPoint` представляет точку в полярной системе координат, состоящей из радиуса `r` и угла `theta`.

Сопутствующий объект `Point` содержит метод `distance`, который вычисляет расстояние между двумя точками. 
Расстояние вычисляется по-разному в зависимости от типов точек:

- Если обе точки являются `CartesianPoint`, то расстояние вычисляется по формуле Евклида в декартовой системе координат.
- Если обе точки являются `PolarPoint`, то расстояние вычисляется по формуле для расстояния между точками в полярной системе координат.
- Если одна точка является `PolarPoint`, а другая является `CartesianPoint`, 
  то расстояние вычисляется по формуле для преобразования координат из декартовой в полярную систему координат 
  и затем вычисляется по формуле для расстояния между точками в полярной системе координат.

**Код:**

```dotty
sealed trait Point

final case class CartesianPoint(x: Double, y: Double) extends Point:
  lazy val toPolarPoint: PolarPoint =
    PolarPoint(math.sqrt(x * x + y * y), math.atan2(y, x))

final case class PolarPoint(r: Double, theta: Double) extends Point:
  lazy val toCartesian: CartesianPoint =
    CartesianPoint(r * math.cos(theta), r * math.sin(theta))

object Point:
  def distance(p1: Point, p2: Point): Double =
    (p1, p2) match
      case (cp1 @ CartesianPoint(_, _), cp2 @ CartesianPoint(_, _)) =>
        distance(cp1, cp2)
      case (pp1 @ PolarPoint(_, _), pp2 @ PolarPoint(_, _)) =>
        distance(pp1, pp2)
      case (pp1 @ PolarPoint(_, _), cp2 @ CartesianPoint(_, _)) =>
        distance(pp1.toCartesian, cp2)
      case (cp1 @ CartesianPoint(_, _), pp2 @ PolarPoint(_, _)) =>
        distance(cp1, pp2.toCartesian)

  private def distance(p1: CartesianPoint, p2: CartesianPoint): Double =
    math.sqrt(math.pow(p1.x - p2.x, 2) + math.pow(p1.y - p2.y, 2))

  private def distance(p1: PolarPoint, p2: PolarPoint): Double =
    math.sqrt(
      p1.r * p1.r + p2.r * p2.r -
        2 * p1.r * p2.r * math.cos(p1.theta - p2.theta)
    )
end Point

val p1 = CartesianPoint(1, 1)
val p2 = CartesianPoint(4, 5)
Point.distance(p1, p2)        // 5.0

// смешанные пары: единый интерфейс, разные формулы под капотом
val p3 = PolarPoint(10, math.Pi / 2)          // точки на оси y с r = 10
Point.distance(p2, p3)                        // расстояние в полярную точку
```

Разберём, как устроен метод `distance`:

1. Сначала по двум переданным точкам находим, с какими конкретными типами мы имеем дело, через сопоставление с шаблоном по кортежу `(p1, p2)`.
2. Для пары однотипных точек вызывается «профильная» частная формула: Евклидово расстояние для двух `CartesianPoint` или формула косинусов для двух `PolarPoint`.
3. Для смешанной пары полярная точка сначала приводится к декартовой через `toCartesian` (или наоборот, через `toPolarPoint`), 
   после чего считается уже однотипное расстояние.

Благодаря сопоставлению с шаблоном по кортежу вызывается *перегруженная* приватная версия `distance`, 
то есть выбирается правильная формула на этапе сопоставления, без ручных формульных разветвлений в общем методе.

## Определение позиции точки по отношению к треугольнику

**Алгоритм:**

Метод `getPointOnTriangleType` определяет, находится ли точка внутри треугольника, на его стороне или вне треугольника. 
Он использует алгоритм, основанный на определении ориентации векторов. 
Если точка находится на одной из сторон треугольника, то результат равен - `OnTheSide`. 
Если точка находится внутри треугольника, то - `Inside`. В противном случае - `Outside`.

**Как это работает.** Для каждой из трёх сторон треугольника вычисляется знаковое произведение двух векторов — это фактически «ориентация» точки относительно данной стороны:

- выражение `(p2.x - p1.x) * (p.y - p1.y) - (p.x - p1.x) * (p2.y - p1.y)` равно нулю, когда точка лежит на прямой, проходящей через сторону;
- знак этого выражения показывает, с какой стороны от стороны находится точка.

Если хотя бы одно из трёх значений равно нулю, точка лежит на стороне (`OnTheSide`). Иначе, если все значения одного знака, то точка находится по одну и ту же сторону от всех сторон — то есть внутри треугольника (`Inside`). В противном случае точка попадает в «разрез» между разными знаками и считается внешней (`Outside`).

Во всех трёх вычислениях вершины берутся в одном и том же фиксированном порядке, поэтому ориентация сторон согласована — требование «все знаки совпадают» корректно определяет внутреннюю область независимо от того, по часовой стрелке или против идут вершины.

Метод `isZeroPointInside` проверяет, находится ли точка с координатами `(0, 0)` внутри треугольника. 
Он использует метод `getPointOnTriangleType` для проверки.

**Код:**

```dotty
final case class Triangle(
    point1: (Int, Int),
    point2: (Int, Int),
    point3: (Int, Int)
):
  import PointOnTriangleType.*

  def getPointOnTriangleType(point: (Int, Int)): PointOnTriangleType =
    val first =
      (point1._1 - point._1) * (point2._2 - point1._2) -
        (point2._1 - point1._1) * (point1._2 - point._2)
    val second =
      (point2._1 - point._1) * (point3._2 - point2._2) -
        (point3._1 - point2._1) * (point2._2 - point._2)
    val third =
      (point3._1 - point._1) * (point1._2 - point3._2) -
        (point1._1 - point3._1) * (point3._2 - point._2)
    if first == 0 || second == 0 || third == 0 then OnTheSide
    else if first < 0 && second < 0 && third < 0 || first > 0 && second > 0 && third > 0
    then Inside
    else Outside

  lazy val isZeroPointInside: Boolean =
    getPointOnTriangleType((0, 0)).equals(Inside)
end Triangle

enum PointOnTriangleType:
  case Inside, Outside, OnTheSide

Triangle((-340, 495), (-153, -910), (835, -947)).getPointOnTriangleType((0, 0))
// Inside
Triangle((-175, 41), (-421, -714), (574, -645)).getPointOnTriangleType((0, 0))
// Outside
Triangle((-175, 41), (-421, -714), (574, -645)).getPointOnTriangleType((-175, 41))
// OnTheSide
```

## Сжатие координат

Часто бывает полезно преобразовать последовательность чисел либо каких-то других объектов 
в промежуток последовательных целых чисел — например, 
чтобы использовать её элементы как индексы в массиве либо какой-нибудь другой структуре.

Эта задача эквивалентна нумерации элементов множества, что можно сделать за \\(O(n)\\) через хеш-таблицу:

```dotty
import scala.collection.mutable

def compress(initial: List[Int]): List[Int] =
  val m = mutable.Map.empty[Int, Int]
  initial.map(i => m.getOrElseUpdate(i, m.size))
  
compress(List(5, 2, 5, 8, 2))
// List(0, 1, 0, 2, 1)  
```

Разберём, как работает эта функция на примере `List(5, 2, 5, 8, 2)`:

- `x = 5` — в таблице ещё нет, назначаем номер `m.size = 0`, получаем `5 → 0`;
- `x = 2` — назначаем номер `1`, получаем `2 → 1`;
- `x = 5` — уже есть, возвращаем `0`;
- `x = 8` — назначаем номер `2`, получаем `8 → 2`;
- `x = 2` — уже есть, возвращаем `1`.

Итог: `List(0, 1, 0, 2, 1)`. Обратите внимание, что `getOrElseUpdate` вычисляет значение-по-умолчанию (`m.size`) лениво, 
только если ключа ещё не было, поэтому номера назначаются именно в порядке первого вхождения.

Элементам будут присвоены номера в порядке их первого вхождения в последовательность. 
Если нужно сохранить *порядок*, присвоив меньшим элементам меньшие номера, то задача становится чуть сложнее, и её можно решить разными способами.

Как вариант, можно отсортировать массив, а затем два раза пройтись по нему с хэш-таблицей — в первый раз заполняя её, а во второй раз сжимая сам массив:

```dotty
def compress(initial: List[Int]): List[Int] =
  val m =
    initial.sorted.foldLeft(Map.empty[Int, Int]) {
      case (acc, i) =>
        acc + (i -> acc.getOrElse(i, acc.size))
    }
  initial.map(i => m(i))  
```

Здесь для `List(5, 2, 5, 8, 2)` отсортированная копия равна `List(2, 2, 5, 5, 8)`. 
При первом проходе мы назначаем `2 → 0`, `5 → 1`, `8 → 2`, а при втором проходе заменяем сами элементы: `5 → 1`, `2 → 0`, `8 → 2`. 
Итог: `List(1, 0, 1, 2, 0)` — теперь меньшим значениям соответствуют меньшие номера.

Также можно выкинуть из отсортированного массива дупликаты (за линейное время), 
а затем использовать его для нахождения индекса каждого элемента исходного массива бинарным поиском:

```dotty
def orderedCompress2(initial: List[Int]): List[Int] =
  val b = initial.sorted.distinct
  initial.map(i => b.search(i).insertionPoint)
```

Для того же примера `b = List(2, 5, 8)`, и каждое значение исходного массива ищется бинарным поиском за \\(O(\log n)\\): 
`5 → 1`, `2 → 0`, `8 → 2`. 
Поскольку все элементы гарантированно присутствуют в `b`, индекс вставки (`insertionPoint`) совпадает с точкой вхождения.

Результат в обоих вариантах одинаковый (по порядку) — собственно, это и есть сжатие координат:

```dotty
compress(List(5, 2, 5, 8, 2)) 
// List(1, 0, 1, 2, 0)
```

Оба подхода работают за \\(O(n \log n)\\).

---

**Ссылки:**

- [Исходный код][sample]
- [Треугольник - Wiki](https://ru.wikipedia.org/wiki/%D0%A2%D1%80%D0%B5%D1%83%D0%B3%D0%BE%D0%BB%D1%8C%D0%BD%D0%B8%D0%BA)
- [Никлаус Вирт - Алгоритмы + Структуры данных = Программы](https://en.wikipedia.org/wiki/Algorithms_%2B_Data_Structures_%3D_Programs)
- [Алгоритмика - Сжатие координат](https://ru.algorithmica.org/cs/sequences/compression/)

[sample]: https://gitverse.ru/scalabook/algorithms
