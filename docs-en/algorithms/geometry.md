# Geometry

## Distance Between Points

**Algorithm:**

Define an abstract data type `Point` and two of its implementations: `CartesianPoint` and `PolarPoint`.
`CartesianPoint` represents a point in the Cartesian coordinate system, consisting of two coordinates `x` and `y`.
`PolarPoint` represents a point in the polar coordinate system, consisting of a radius `r` and an angle `theta`.

The companion object `Point` contains a `distance` method that computes the distance between two points.
The distance is computed differently depending on the types of points:

- If both points are `CartesianPoint`, the distance is computed using the Euclidean formula in Cartesian coordinates.
- If both points are `PolarPoint`, the distance is computed using the formula for the distance between points in polar coordinates.
- If one point is `PolarPoint` and the other is `CartesianPoint`,
  the distance is computed by transforming coordinates from Cartesian to polar coordinate system
  and then using the formula for the distance between points in polar coordinates.

**Code:**

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

// mixed pairs: unified interface, different formulas under the hood
val p3 = PolarPoint(10, math.Pi / 2)          // points on the y-axis with r = 10
Point.distance(p2, p3)                        // distance to polar point
```

Let us examine how the `distance` method works:

1. First, based on the two provided points, we determine which concrete types we are dealing with, through pattern matching on the tuple `(p1, p2)`.
2. For a pair of points of the same type, a "specialized" private formula is called: the Euclidean distance for two `CartesianPoint` values, or the law of cosines for two `PolarPoint` values.
3. For a mixed pair, the polar point is first converted to Cartesian via `toCartesian` (or vice versa via `toPolarPoint`),
   after which the distance between points of the same type is computed.

Thanks to pattern matching on the tuple, the *overloaded* private version of `distance` is called,
meaning the correct formula is selected at compile-time pattern matching, without manual formula branching in the general method.

## Point Position Relative to a Triangle

**Algorithm:**

The method `getPointOnTriangleType` determines whether a point is inside a triangle, on one of its sides, or outside the triangle.
It uses an algorithm based on determining the orientation of vectors.
If the point is on one of the sides of a triangle, the result is - `OnTheSide`.
If the point is inside the triangle, the result is - `Inside`. Otherwise — `Outside`.

**How it works.** For each of the three sides of a triangle, a signed cross product of two vectors is computed — this is essentially the "orientation" of the point relative to that side:

- the expression `(p2.x - p1.x) * (p.y - p1.y) - (p.x - p1.x) * (p2.y - p1.y)` equals zero when the point lies on the line passing through the side;
- the sign of this expression shows which side of the side the point is on.

If at least one of the three values equals zero, the point lies on a side (`OnTheSide`). Otherwise, if all values have the same sign, the point lies on the same side of all sides — i.e., inside the triangle (`Inside`). In the other case, the point falls into a "split" between different signs and is considered external (`Outside`).

In all three calculations, the vertices are taken in the same fixed order, so the orientations of the sides are consistent — the requirement "all signs match" correctly identifies the interior region regardless of whether the vertices are clockwise or counter-clockwise.

The method `isZeroPointInside` checks whether the point with coordinates `(0, 0)` is inside the triangle.
It uses the `getPointOnTriangleType` method for checking.

**Code:**

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

## Coordinate Compression

It is often useful to transform a sequence of numbers or some other objects
into a sequence of consecutive integers — for example,
to use their elements as array indexes or some other data structure.

This problem is equivalent to numbering elements of a set, which can be done in \\(O(n)\\) through a hash table:

```dotty
import scala.collection.mutable

def compress(initial: List[Int]): List[Int] =
  val m = mutable.Map.empty[Int, Int]
  initial.map(i => m.getOrElseUpdate(i, m.size))
  
compress(List(5, 2, 5, 8, 2))
// List(0, 1, 0, 2, 1)  
```

Let us examine how this function works with the example `List(5, 2, 5, 8, 2)`:

- `x = 5` — not yet in the table, assign number `m.size = 0`, we get `5 → 0`;
- `x = 2` — assign number `1`, we get `2 → 1`;
- `x = 5` — already exists, return `0`;
- `x = 8` — assign number `2`, we get `8 → 2`;
- `x = 2` — already exists, return `1`.

Result: `List(0, 1, 0, 2, 1)`. Note that `getOrElseUpdate` computes the default value (`m.size`) lazily,
only if the key was not present, so numbers are assigned in the order of first occurrence.

Elements will be numbered in the order of their first occurrence in the sequence.
If we want to preserve the *order*, assigning smaller values smaller numbers, the problem becomes slightly more complex and can be solved in different ways.

For instance, we can sort the array and then pass through it twice with a hash table — first to fill it, and second to compress the array itself:

```dotty
def compress(initial: List[Int]): List[Int] =
  val m =
    initial.sorted.foldLeft(Map.empty[Int, Int]) {
      case (acc, i) =>
        acc + (i -> acc.getOrElse(i, acc.size))
    }
  initial.map(i => m(i))  
```

Here for `List(5, 2, 5, 8, 2)` the sorted copy is `List(2, 2, 5, 5, 8)`.
During the first pass we assign `2 → 0`, `5 → 1`, `8 → 2`, and during the second pass we replace the elements themselves: `5 → 1`, `2 → 0`, `8 → 2`.
Result: `List(1, 0, 1, 2, 0)` — now smaller values correspond to smaller numbers.

We can also remove duplicates from the sorted array (in linear time),
and then use it to find the index of each element of the original array using binary search:

```dotty
def orderedCompress2(initial: List[Int]): List[Int] =
  val b = initial.sorted.distinct
  initial.map(i => b.search(i).insertionPoint)
```

For the same example `b = List(2, 5, 8)`, and each value of the original array is found using binary search in \\(O(\log n)\\):
`5 → 1`, `2 → 0`, `8 → 2`.
Since all elements are guaranteed to be present in `b`, the insertion index (`insertionPoint`) matches the actual index.

The results in both variants are the same (by order) — this is precisely coordinate compression:

```dotty
compress(List(5, 2, 5, 8, 2)) 
// List(1, 0, 1, 2, 0)
```

Both approaches run in \\(O(n \log n)\\).

---

**References:**

- [Source code][sample]
- [Triangle - Wiki](https://en.wikipedia.org/wiki/Triangle)
- [Niklaus Wirth - Algorithms + Data Structures = Programs](https://en.wikipedia.org/wiki/Algorithms_%2B_Data_Structures_%3D_Programs)
- [Algorithmica - Coordinate Compression](https://ru.algorithmica.org/cs/sequences/compression/)

[sample]: https://gitverse.ru/scalabook/algorithms
