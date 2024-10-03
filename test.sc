sealed trait Tree
case object Empty                                                extends Tree
case class Node(entry: Int, leftBranch: Tree, rightBranch: Tree) extends Tree

object Tree:
  def makeTree(entry: Int, leftBranch: Tree, rightBranch: Tree): Tree =
    Node(entry, leftBranch, rightBranch)

def elementOfSet(tree: Tree, element: Int): Boolean =
  tree match
    case Empty                                 => false
    case Node(entry, _, _) if entry == element => true
    case Node(entry, leftBranch, _) if element < entry =>
      elementOfSet(leftBranch, element)
    case Node(entry, _, rightBranch) =>
      elementOfSet(rightBranch, element)

println("elementOfSet")
println(elementOfSet(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)), 3))
println(elementOfSet(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)), 5))
println(elementOfSet(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)), 7))
println(elementOfSet(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)), 10))
println(elementOfSet(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)), 1))

def adjoinSet(tree: Tree, element: Int): Tree =
  tree match
    case Empty                                 => Node(element, Empty, Empty)
    case Node(entry, _, _) if entry == element => tree
    case Node(entry, leftBranch, rightBranch) if element < entry =>
      Node(entry, adjoinSet(leftBranch, element), rightBranch)
    case Node(entry, leftBranch, rightBranch) =>
      Node(entry, leftBranch, adjoinSet(rightBranch, element))

println("adjoinSet")
println(adjoinSet(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)), 3))
println(adjoinSet(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)), 5))
println(adjoinSet(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)), 7))
println(adjoinSet(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)), 10))
println(adjoinSet(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)), 1))
println(adjoinSet(Empty, 5))
println(adjoinSet(Node(5, Empty, Empty), 3))
println(adjoinSet(Node(5, Empty, Empty), 7))

def treeToList1(tree: Tree): List[Int] =
  tree match
    case Empty => Nil
    case Node(entry, leftBranch, rightBranch) =>
      treeToList1(leftBranch) ++ (entry :: treeToList1(rightBranch))

println("treeToList1")
println(treeToList1(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty))))
println(treeToList1(Node(
  5,
  Node(3, Node(2, Empty, Empty), Node(4, Empty, Empty)),
  Node(7, Empty, Node(8, Empty, Empty))
)))
println(treeToList1(Empty))
println(treeToList1(Node(
  5,
  Node(3, Node(1, Empty, Empty), Empty),
  Node(9, Node(7, Empty, Empty), Node(11, Empty, Empty))
)))

def treeToList2(tree: Tree): List[Int] =
  def copyToList(tree: Tree, resultList: List[Int]): List[Int] =
    tree match
      case Empty => resultList
      case Node(entry, leftBranch, rightBranch) =>
        copyToList(leftBranch, entry :: copyToList(rightBranch, resultList))

  copyToList(tree, Nil)

println("treeToList2")
println(treeToList2(Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty))))
println(treeToList2(Node(
  5,
  Node(3, Node(2, Empty, Empty), Node(4, Empty, Empty)),
  Node(7, Empty, Node(8, Empty, Empty))
)))
println(treeToList2(Empty))
println(treeToList2(Node(
  5,
  Node(3, Node(1, Empty, Empty), Empty),
  Node(9, Node(7, Empty, Empty), Node(11, Empty, Empty))
)))

def listToTree(elements: List[Int]): Tree =
  partialTree(elements, elements.length)._1

def partialTree(elements: List[Int], n: Int): (Tree, List[Int]) =
  if n == 0 then (Empty, elements)
  else
    val leftSize                    = (n - 1) / 2
    val (leftTree, nonLeftElements) = partialTree(elements, leftSize)
    val thisEntry                   = nonLeftElements.head
    val rightSize                   = n - (leftSize + 1)
    val (rightTree, remainingElements) =
      partialTree(nonLeftElements.tail, rightSize)
    (Node(thisEntry, leftTree, rightTree), remainingElements)

println("partialTree")
println(partialTree(List(1, 2, 3, 4, 5, 6, 7), 7))
println(partialTree(List(1, 2, 3, 4, 5, 6, 7), 4))
println(partialTree(List(1, 2, 3, 4, 5, 6, 7, 8, 9), 9))
println(partialTree(List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 10))

println("listToTree")
println(listToTree(List(1, 3, 5, 7, 9, 11)))

def unionSet[A: Ordering](set1: List[A], set2: List[A]): List[A] =
  val ord = summon[Ordering[A]]
  (set1, set2) match
    case (Nil, _) => set2
    case (_, Nil) => set1
    case (head1 :: tail1, head2 :: tail2) if ord.equiv(head1, head2) =>
      head1 :: unionSet(tail1, tail2)
    case (head1 :: tail1, head2 :: _) if ord.lt(head1, head2) =>
      head1 :: unionSet(tail1, set2)
    case (_, head2 :: tail2) =>
      head2 :: unionSet(set1, tail2)

def unionSet(tree1: Tree, tree2: Tree): Tree =
  listToTree(unionSet[Int](treeToList1(tree1), treeToList1(tree2)))

println("unionSet")
println(unionSet(
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)),
  Node(5, Node(3, Empty, Empty), Node(9, Empty, Empty))
))
println(unionSet(
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)),
  Node(1, Node(3, Empty, Empty), Node(9, Empty, Empty))
))
println(unionSet(
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)),
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Node(9, Empty, Empty)))
))
println(unionSet(
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)),
  Node(5, Node(3, Empty, Node(4, Empty, Empty)), Node(7, Empty, Empty))
))
println(unionSet(
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)),
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty))
))

def intersectionSet[A: Ordering](set1: List[A], set2: List[A]): List[A] =
  val ord = summon[Ordering[A]]
  (set1, set2) match
    case (Nil, _) => Nil
    case (_, Nil) => Nil
    case (head1 :: tail1, head2 :: tail2) if ord.equiv(head1, head2) =>
      head1 :: intersectionSet(tail1, tail2)
    case (head1 :: tail1, head2 :: _) if ord.lt(head1, head2) =>
      intersectionSet(tail1, set2)
    case (_, _ :: tail2) =>
      intersectionSet(set1, tail2)

def intersectionSet(tree1: Tree, tree2: Tree): Tree =
  listToTree(intersectionSet[Int](treeToList1(tree1), treeToList1(tree2)))

println("intersectionSet")
println(intersectionSet(
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)),
  Node(5, Node(3, Empty, Empty), Node(9, Empty, Empty))
))
println(intersectionSet(
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)),
  Node(1, Node(3, Empty, Empty), Node(9, Empty, Empty))
))
println(intersectionSet(
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)),
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Node(9, Empty, Empty)))
))
println(intersectionSet(
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)),
  Node(5, Node(3, Empty, Node(4, Empty, Empty)), Node(7, Empty, Empty))
))
println(intersectionSet(
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty)),
  Node(5, Node(3, Empty, Empty), Node(7, Empty, Empty))
))
