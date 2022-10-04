package algorithms.trees

enum BinaryTree[+A]:
  case Leaf
  case Branch(value: A, left: BinaryTree[A], right: BinaryTree[A])

  lazy val size: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + l.size + r.size

  lazy val depth: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + l.depth.max(r.depth)

object BinaryTree:
  def apply[A](list: List[A]): BinaryTree[A] =
    list match
      case Nil => Leaf
      case x :: xs =>
        val (leftList, rightList) = xs.splitAt(xs.length / 2)
        Branch(x, BinaryTree(leftList), BinaryTree(rightList))

  def preorder[A](binTree: BinaryTree[A]): IndexedSeq[A] =
    binTree match
      case Leaf                       => IndexedSeq.empty
      case Branch(value, left, right) => IndexedSeq(value) ++ preorder(left) ++ preorder(right)

  def inorder[A](binTree: BinaryTree[A]): IndexedSeq[A] =
    binTree match
      case Leaf                                   => IndexedSeq.empty
      case Branch(value, leftBranch, rightBranch) => inorder(leftBranch) ++ IndexedSeq(value) ++ inorder(rightBranch)

  def postorder[A](binTree: BinaryTree[A]): IndexedSeq[A] =
    binTree match
      case Leaf => IndexedSeq.empty
      case Branch(value, leftBranch, rightBranch) =>
        postorder(leftBranch) ++ postorder(rightBranch) ++ IndexedSeq(value)
