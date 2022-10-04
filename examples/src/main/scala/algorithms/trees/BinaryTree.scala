package algorithms.trees

enum BinaryTree[+A]:
  case Leaf
  case Branch(value: A, left: BinaryTree[A], right: BinaryTree[A])

  def size: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + l.size + r.size

  def depth: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + l.depth.max(r.depth)

object BinaryTree:
  def apply[A](list: List[A]): BinaryTree[A] =
    list match
      case Nil => Leaf
      case x :: xs =>
        val (leftList, rightList) = xs.splitAt(xs.length / 2)
        Branch(x, BinaryTree(leftList), BinaryTree(rightList))
