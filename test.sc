import scala.annotation.tailrec
import scala.math.Ordered.orderingToOrdered

def selectionSearch[A: Ordering](list: List[A], n: Int): Option[A] =
  @tailrec
  def partitionAndSearch(as: List[A], m: Int): Option[A] = as match
    case Nil => None
    case h :: t =>
      val (l, p, g) = partition(h, t)
      val len       = l.length
      if len == m - 1 then Some(p)
      else if len >= m then partitionAndSearch(l, m)
      else partitionAndSearch(g, m - len - 1)

  def partition(head: A, tail: List[A]): (List[A], A, List[A]) =
    @tailrec
    def loop(
        rest: List[A],
        less: List[A],
        p: A,
        more: List[A]
    ): (List[A], A, List[A]) =
      rest match
        case Nil             => (less, p, more)
        case h :: t if h < p => loop(t, h :: less, p, more)
        case h :: t          => loop(t, less, p, h :: more)

    loop(tail, Nil, head, Nil)

  if n <= 0 then None
  else partitionAndSearch(list, n)
end selectionSearch

println(selectionSearch((1 to 100).toList, 45))
