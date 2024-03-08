package ru.scalabook.algorithms.trees

import cats.data.NonEmptyList

import scala.annotation.tailrec

enum BinaryTree[+A]:
  case Leaf
  case Branch(value: A, left: BinaryTree[A], right: BinaryTree[A])

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  lazy val size: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + l.size + r.size

  lazy val height: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + math.max(l.height, r.height)

  def rootPath[B >: A](b: B): Option[NonEmptyList[B]] = this match
    case Leaf                              => None
    case Branch(value, _, _) if value == b => Some(NonEmptyList.one(b))
    case Branch(value, left, right) =>
      left.rootPath(b).orElse(right.rootPath(b)).map: nel =>
        value :: nel

  def level(level: Int): List[A] =
    @tailrec
    def loop(level: Int, trees: List[BinaryTree[A]]): List[A] =
      if level < 0 then List.empty
      else if level == 0 then
        trees.foldLeft(List.empty[A]) { case (acc, tree) =>
          tree match
            case Leaf            => acc
            case Branch(v, _, _) => v :: acc
        }
      else
        loop(
          level - 1,
          trees.flatMap:
            case Branch(_, left, right) if !left.isEmpty && !right.isEmpty =>
              List(left, right)
            case Branch(_, left, _) if !left.isEmpty   => List(left)
            case Branch(_, _, right) if !right.isEmpty => List(right)
            case _ => List.empty[BinaryTree[A]]
        )

    loop(level, List(this))
  end level

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
end BinaryTree

object BinaryTree:
  def apply[A](a: A): BinaryTree[A] = Branch(a, Leaf, Leaf)

  def apply[A](values: List[A]): BinaryTree[A] =
    values match
      case Nil => Leaf
      case x :: xs =>
        val (left, right) = xs.splitAt(xs.length / 2)
        Branch(x, BinaryTree(left), BinaryTree(right))

  def apply[A](values: Vector[A]): BinaryTree[A] =
    if values.isEmpty then Leaf
    else
      val (left, right) = values.tail.splitAt(values.length / 2)
      Branch(values.head, BinaryTree(left), BinaryTree(right))

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
end BinaryTree
