import scala.annotation.tailrec
import scala.math.Ordered.orderingToOrdered

def binarySearch[A: Ordering](list: List[A], key: A): Option[A] =
  @tailrec
  def search(lst: List[A], sub: List[A]): Option[A] =
    if lst == sub then None
    else
      middle(lst, sub) match
        case Nil => None
        case head :: next =>
          if key < head then search(lst, head :: next)
          else if key > head then search(next, sub)
          else Some(head)

  def middle(lst: List[A], sub: List[A]): List[A] =
    @tailrec
    def race(oneStep: List[A], twoSteps: List[A]): List[A] =
      if twoSteps.nonEmpty && twoSteps != sub && twoSteps.tail != sub then
        race(oneStep.drop(1), twoSteps.drop(2))
      else oneStep

    race(lst, lst)

  search(list, Nil)
end binarySearch

println(binarySearch(List(1, 3, 5, 7, 9), 3))
println(binarySearch((1 to 100).toList, 45))
