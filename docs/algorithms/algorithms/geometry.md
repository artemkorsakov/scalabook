# Геометрия

## Определение позиции точки по отношению к треугольнику

**Алгоритм:**

Пусть задан треугольник тремя вершинами.
Вычислить, где лежит заданная точка по отношению к заданному треугольнику: снаружи, внутри или на одной из сторон.

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

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

**Метрики:**

```dotty
@main def getPointOnTriangleTypeBench: PointOnTriangleType =
  Triangle((-340, 495), (-153, -910), (835, -947)).getPointOnTriangleType((0, 0))

// CPU Time: 180 ms
// Allocation memory size: 7,89 MB      
```

---

**Ссылки:**

- [Треугольник - Wiki](https://ru.wikipedia.org/wiki/%D0%A2%D1%80%D0%B5%D1%83%D0%B3%D0%BE%D0%BB%D1%8C%D0%BD%D0%B8%D0%BA)
