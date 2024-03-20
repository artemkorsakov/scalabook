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
  def toPolarPoint: PolarPoint =
    PolarPoint(math.sqrt(x * x + y * y), math.atan2(y, x))

final case class PolarPoint(r: Double, theta: Double) extends Point:
  def toCartesian: CartesianPoint =
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
Point.distance(p1, p2)        // 5
```

## Определение позиции точки по отношению к треугольнику

**Алгоритм:**

Метод `getPointOnTriangleType` определяет, находится ли точка внутри треугольника, на его стороне или вне треугольника. 
Он использует алгоритм, основанный на определении ориентации векторов. 
Если точка находится на одной из сторон треугольника, то результат равен - `OnTheSide`. 
Если точка находится внутри треугольника, то - `Inside`. В противном случае - `Outside`.

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
      (point1._1 - point._1) * (point2._2 - point1._2) - (point2._1 - point1._1) * (point1._2 - point._2)
    val second =
      (point2._1 - point._1) * (point3._2 - point2._2) - (point3._1 - point2._1) * (point2._2 - point._2)
    val third =
      (point3._1 - point._1) * (point1._2 - point3._2) - (point1._1 - point3._1) * (point3._2 - point._2)
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

---

**Ссылки:**

- [Треугольник - Wiki](https://ru.wikipedia.org/wiki/%D0%A2%D1%80%D0%B5%D1%83%D0%B3%D0%BE%D0%BB%D1%8C%D0%BD%D0%B8%D0%BA)
- [Никлаус Вирт - Алгоритмы + Структуры данных = Программы](https://en.wikipedia.org/wiki/Algorithms_%2B_Data_Structures_%3D_Programs)