package ru.scalabook.algorithms.trees

import weaver.FunSuite

object HuffmanTreeSuite extends FunSuite:
  test("makeCodeTree with two leaves"):
    val left  = HuffmanLeaf('A', 1)
    val right = HuffmanLeaf('B', 2)
    val tree  = HuffmanTree.makeCodeTree(left, right)
    expect.all(
      tree.symbols == Vector('A', 'B'),
      tree.weight == 3,
      tree.left == left,
      tree.right == right
    )

  test("makeCodeTree with leaf and branch"):
    val left = HuffmanLeaf('A', 1)
    val right =
      HuffmanTree.makeCodeTree(HuffmanLeaf('B', 2), HuffmanLeaf('C', 3))
    val tree = HuffmanTree.makeCodeTree(left, right)
    expect.all(
      tree.symbols == Vector('A', 'B', 'C'),
      tree.weight == 6,
      tree.left == left,
      tree.right == right
    )

  test("makeCodeTree with two branches"):
    val left =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val right =
      HuffmanTree.makeCodeTree(HuffmanLeaf('C', 3), HuffmanLeaf('D', 4))
    val tree = HuffmanTree.makeCodeTree(left, right)
    expect.all(
      tree.symbols == Vector('A', 'B', 'C', 'D'),
      tree.weight == 10,
      tree.left == left,
      tree.right == right
    )

  import HuffmanTree.Bit

  test("decode with multiple symbols"):
    val tree =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val bits    = List(Bit.Zero, Bit.One)
    val decoded = HuffmanTree.decode(bits, tree)
    expect(decoded == Vector('A', 'B'))

  test("decode with repeated symbols"):
    val tree =
      HuffmanTree.makeCodeTree(HuffmanLeaf('C', 1), HuffmanLeaf('D', 2))
    val bits    = List(Bit.Zero, Bit.Zero, Bit.One)
    val decoded = HuffmanTree.decode(bits, tree)
    expect(decoded == Vector('C', 'C', 'D'))

  test("decode with empty encoded list"):
    val tree =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val bits    = List()
    val decoded = HuffmanTree.decode(bits, tree)
    expect(decoded == Vector())
end HuffmanTreeSuite
