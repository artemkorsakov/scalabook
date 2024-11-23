import scala.annotation.tailrec
import scala.math.Ordered.orderingToOrdered

def quickSort[A: Ordering](list: List[A]): List[A] =
  sealed trait Queue[A]
  case class Element[A](head: A)    extends Queue[A]
  case class Line[A](list: List[A]) extends Queue[A]

  @tailrec
  def partitionAndSort(
      list: List[A],
      sorted: List[A],
      unsorted: List[Queue[A]]
  ): List[A] =
    (list, unsorted) match
      case (Nil, Nil)      => sorted
      case (h :: Nil, Nil) => h :: sorted
      case (Nil, hq :: tq) =>
        hq match
          case Element(el) =>
            partitionAndSort(Nil, el :: sorted, tq)
          case Line(list) =>
            partitionAndSort(list, sorted, tq)
      case (h :: Nil, hq :: tq) =>
        hq match
          case Element(el) =>
            partitionAndSort(Nil, el :: h :: sorted, tq)
          case Line(list) =>
            partitionAndSort(list, h :: sorted, tq)
      case (h :: t, _) =>
        val (l, p, g) = partition(h, t)
        partitionAndSort(l, sorted, Element(p) :: Line(g) :: unsorted)

  def partition(p: A, rest: List[A]): (List[A], A, List[A]) =
    @tailrec
    def loop(
        p: A,
        rest: List[A],
        less: List[A],
        more: List[A]
    ): (List[A], A, List[A]) =
      rest match
        case Nil             => (less, p, more)
        case h :: t if h < p => loop(p, t, h :: less, more)
        case h :: t          => loop(p, t, less, h :: more)

    loop(p, rest, Nil, Nil)

  partitionAndSort(list, Nil, Nil).reverse
end quickSort

println(quickSort(List(3, 1, 4, 2)))
