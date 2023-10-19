package ru.scalabook.algorithms.trees

enum BinaryTree[+A]:
  case Leaf
  case Branch(value: A, left: BinaryTree[A], right: BinaryTree[A])

  lazy val size: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + l.size + r.size

  lazy val depth: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + l.depth.max(r.depth)

  lazy val preorder: IndexedSeq[A] = this match
    case Leaf => IndexedSeq.empty
    case Branch(value, left, right) =>
      IndexedSeq(value) ++ left.preorder ++ right.preorder

  lazy val inorder: IndexedSeq[A] = this match
    case Leaf => IndexedSeq.empty
    case Branch(value, left, right) =>
      left.inorder ++ IndexedSeq(value) ++ right.inorder

  lazy val postorder: IndexedSeq[A] = this match
    case Leaf => IndexedSeq.empty
    case Branch(value, left, right) =>
      left.postorder ++ right.postorder ++ IndexedSeq(value)

object BinaryTree:
  def apply[A](a: A): BinaryTree[A] = Branch(a, Leaf, Leaf)

  def apply[A](list: List[A]): BinaryTree[A] =
    list match
      case Nil => Leaf
      case x :: xs =>
        val (left, right) = xs.splitAt(xs.length / 2)
        Branch(x, BinaryTree(left), BinaryTree(right))

  def areSimilar[A, B](treeA: BinaryTree[A], treeB: BinaryTree[B]): Boolean =
    (treeA, treeB) match
      case (Leaf, Leaf) => true
      case (Branch(_, leftA, rightA), Branch(_, leftB, rightB)) =>
        areSimilar(leftA, leftB) && areSimilar(rightA, rightB)
      case _ => false

  def areEqual[A](treeA: BinaryTree[A], treeB: BinaryTree[A]): Boolean =
    (treeA, treeB) match
      case (Leaf, Leaf) => true
      case (Branch(valueA, leftA, rightA), Branch(valueB, leftB, rightB)) =>
        valueA == valueB && areEqual(leftA, leftB) && areEqual(rightA, rightB)
      case _ => false
