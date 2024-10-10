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

  test("adjoinSet with two leaves"):
    val leaf1  = HuffmanLeaf('A', 1)
    val leaf2  = HuffmanLeaf('B', 2)
    val set    = List(leaf1)
    val result = HuffmanTree.adjoinSet(leaf2, set)
    expect(result == List(leaf1, leaf2))

  test("adjoinSet with leaf and branch"):
    val leaf = HuffmanLeaf('A', 1)
    val branch =
      HuffmanTree.makeCodeTree(HuffmanLeaf('B', 2), HuffmanLeaf('C', 3))
    val set    = List(branch)
    val result = HuffmanTree.adjoinSet(leaf, set)
    expect(result == List(leaf, branch))

  test("adjoinSet with two branches"):
    val branch1 =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val branch2 =
      HuffmanTree.makeCodeTree(HuffmanLeaf('C', 3), HuffmanLeaf('D', 4))
    val set    = List(branch1)
    val result = HuffmanTree.adjoinSet(branch2, set)
    expect(result == List(branch1, branch2))

  test("adjoinSet with empty set"):
    val leaf   = HuffmanLeaf('A', 1)
    val set    = List()
    val result = HuffmanTree.adjoinSet(leaf, set)
    expect(result == List(leaf))

  test("makeLeafSet with one symbol"):
    val symbols = List(('A', 1))
    val result  = HuffmanTree.makeLeafSet(symbols)
    expect(result == List(HuffmanLeaf('A', 1)))

  test("makeLeafSet with multiple symbols"):
    val symbols = List(('B', 2), ('A', 1), ('C', 3))
    val result  = HuffmanTree.makeLeafSet(symbols)
    expect(result == List(
      HuffmanLeaf('A', 1),
      HuffmanLeaf('B', 2),
      HuffmanLeaf('C', 3)
    ))

  test("makeLeafSet with empty list"):
    val symbols = List()
    val result  = HuffmanTree.makeLeafSet(symbols)
    expect(result == List())

  test("encodeSymbol with single symbol in the leaf"):
    val tree    = HuffmanLeaf('A', 1)
    val encoded = HuffmanTree.encodeSymbol('A', tree)
    whenSuccess(encoded): actual =>
      expect(actual.isEmpty)

  test("encodeSymbol with single symbol in the left"):
    val tree =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val encoded = HuffmanTree.encodeSymbol('A', tree)
    whenSuccess(encoded): actual =>
      expect(actual == List(Bit.Zero))

  test("encodeSymbol with single symbol in the right"):
    val tree =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val encoded = HuffmanTree.encodeSymbol('B', tree)
    whenSuccess(encoded): actual =>
      expect(actual == List(Bit.One))

  test("encodeSymbol with multiple symbols"):
    val tree = HuffmanTree.makeCodeTree(
      HuffmanLeaf('A', 1),
      HuffmanTree.makeCodeTree(HuffmanLeaf('B', 2), HuffmanLeaf('C', 3))
    )
    val encodedA = HuffmanTree.encodeSymbol('A', tree)
    val encodedB = HuffmanTree.encodeSymbol('B', tree)
    val encodedC = HuffmanTree.encodeSymbol('C', tree)
    expect.all(
      encodedA == Right(List(Bit.Zero)),
      encodedB == Right(List(Bit.One, Bit.Zero)),
      encodedC == Right(List(Bit.One, Bit.One))
    )

  test("encodeSymbol with non-existent symbol in the leaf"):
    val tree    = HuffmanLeaf('A', 1)
    val encoded = HuffmanTree.encodeSymbol('B', tree)
    expect(encoded.isLeft)

  test("encodeSymbol with non-existent symbol"):
    val tree =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val encoded = HuffmanTree.encodeSymbol('C', tree)
    expect(encoded.isLeft)

  test("encode with single symbol"):
    val tree    = HuffmanLeaf('A', 1)
    val message = List('A')
    val encoded = HuffmanTree.encode(message, tree)
    expect(encoded == Right(List()))

  test("encode with multiple symbols"):
    val tree =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val message = List('A', 'B')
    val encoded = HuffmanTree.encode(message, tree)
    expect(encoded == Right(List(Bit.Zero, Bit.One)))

  test("encode with repeated symbols"):
    val tree =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val message = List('A', 'A', 'B')
    val encoded = HuffmanTree.encode(message, tree)
    expect(encoded == Right(List(Bit.Zero, Bit.Zero, Bit.One)))

  test("encode with non-existent symbol"):
    val tree =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val message = List('A', 'C')
    expect(HuffmanTree.encode(message, tree).isLeft)

  test("encode with empty message"):
    val tree =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val message = List()
    expect(HuffmanTree.encode(message, tree) == Right(List()))

  test("decode and encode the example"):
    val tree =
      HuffmanTree.makeCodeTree(
        HuffmanLeaf('A', 4),
        HuffmanTree.makeCodeTree(
          HuffmanLeaf('B', 2),
          HuffmanTree.makeCodeTree(HuffmanLeaf('D', 1), HuffmanLeaf('C', 1))
        )
      )
    val bits = List(
      Bit.Zero,
      Bit.One,
      Bit.One,
      Bit.Zero,
      Bit.Zero,
      Bit.One,
      Bit.Zero,
      Bit.One,
      Bit.Zero,
      Bit.One,
      Bit.One,
      Bit.One,
      Bit.Zero
    )
    val message = List('A', 'D', 'A', 'B', 'B', 'C', 'A')
    val decoded = HuffmanTree.decode(bits, tree)
    val encoded = HuffmanTree.encode(message, tree)
    expect.all(
      decoded == message.toVector,
      encoded == Right(bits)
    )

  test("successiveMerge with two leaves"):
    val leaf1  = HuffmanLeaf('A', 1)
    val leaf2  = HuffmanLeaf('B', 2)
    val set    = List(leaf1, leaf2)
    val result = HuffmanTree.successiveMerge(set)
    expect(result.contains(HuffmanTree.makeCodeTree(leaf1, leaf2)))

  test("successiveMerge with leaf and branch"):
    val leaf = HuffmanLeaf('A', 1)
    val branch =
      HuffmanTree.makeCodeTree(HuffmanLeaf('B', 2), HuffmanLeaf('C', 3))
    val set    = List(leaf, branch)
    val result = HuffmanTree.successiveMerge(set)
    expect(result.contains(HuffmanTree.makeCodeTree(leaf, branch)))

  test("successiveMerge with two branches"):
    val branch1 =
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    val branch2 =
      HuffmanTree.makeCodeTree(HuffmanLeaf('C', 3), HuffmanLeaf('D', 4))
    val set    = List(branch1, branch2)
    val result = HuffmanTree.successiveMerge(set)
    expect(result.contains(HuffmanTree.makeCodeTree(branch1, branch2)))

  test("successiveMerge with empty set"):
    val set    = List()
    val result = HuffmanTree.successiveMerge(set)
    expect(result.isEmpty)

  test("successiveMerge with single element set"):
    val leaf   = HuffmanLeaf('A', 1)
    val set    = List(leaf)
    val result = HuffmanTree.successiveMerge(set)
    expect(result.contains(leaf))

  test("successiveMerge with multiple elements set"):
    val leaf1  = HuffmanLeaf('A', 1)
    val leaf2  = HuffmanLeaf('B', 2)
    val leaf3  = HuffmanLeaf('C', 3)
    val set    = List(leaf1, leaf2, leaf3)
    val result = HuffmanTree.successiveMerge(set)
    expect(result.contains(HuffmanTree.makeCodeTree(
      leaf3,
      HuffmanTree.makeCodeTree(leaf1, leaf2)
    )))

  test("generateHuffmanTree with one symbol"):
    val symbols = List(('A', 1))
    val tree    = HuffmanTree.generateHuffmanTree(symbols)
    expect(tree.contains(HuffmanLeaf('A', 1)))

  test("generateHuffmanTree with multiple symbols"):
    val symbols = List(('B', 2), ('A', 1), ('C', 3))
    val tree    = HuffmanTree.generateHuffmanTree(symbols)
    expect(tree.contains(HuffmanTree.makeCodeTree(
      HuffmanLeaf('C', 3),
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    )))

  test("generateHuffmanTree with empty list"):
    val symbols = List()
    val tree    = HuffmanTree.generateHuffmanTree(symbols)
    expect(tree.isEmpty)

  test("generateHuffmanTree with single symbol and weight"):
    val symbols = List(('A', 5))
    val tree    = HuffmanTree.generateHuffmanTree(symbols)
    expect(tree.contains(HuffmanLeaf('A', 5)))

  test("generateHuffmanTree with multiple symbols and weights"):
    val symbols = List(('B', 2), ('A', 1), ('C', 3))
    val tree    = HuffmanTree.generateHuffmanTree(symbols)
    expect(tree.contains(HuffmanTree.makeCodeTree(
      HuffmanLeaf('C', 3),
      HuffmanTree.makeCodeTree(HuffmanLeaf('A', 1), HuffmanLeaf('B', 2))
    )))

end HuffmanTreeSuite
