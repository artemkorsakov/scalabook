import scala.annotation.tailrec
import scala.math.Ordered.orderingToOrdered

def quickSort[A: Ordering](list: List[A]): List[A] =
  def partitionAndSort(list: List[A]): List[A] =
    list match
      case Nil      => list
      case h :: Nil => list
      case h :: t =>
        val (l, p, g) = partition(h, t)
        partitionAndSort(l) ::: (p :: partitionAndSort(g))

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

  partitionAndSort(list)
end quickSort

println(quickSort(List(3, 1, 4, 2)))
