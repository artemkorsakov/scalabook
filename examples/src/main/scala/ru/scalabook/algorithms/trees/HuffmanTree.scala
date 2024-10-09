package ru.scalabook.algorithms.trees

sealed trait HuffmanTree:
  def symbols: Vector[Char]
  def weight: Int

case class HuffmanLeaf(symbol: Char, weight: Int) extends HuffmanTree:
  val symbols = Vector(symbol)
case class HuffmanBranch(
    symbols: Vector[Char],
    weight: Int,
    left: HuffmanTree,
    right: HuffmanTree
) extends HuffmanTree

object HuffmanTree:
  def makeCodeTree(left: HuffmanTree, right: HuffmanTree): HuffmanBranch =
    HuffmanBranch(
      symbols = left.symbols ++ right.symbols,
      weight = left.weight + right.weight,
      left = left,
      right = right
    )

  enum Bit:
    case Zero, One

  def decode(bits: List[Bit], tree: HuffmanBranch): Vector[Char] =
    def decodeLoop(
        bits: List[Bit],
        currentBranch: HuffmanTree,
        message: Vector[Char]
    ): Vector[Char] =
      (bits, currentBranch) match
        case (_, HuffmanLeaf(symbol, _)) =>
          decodeLoop(bits, tree, message :+ symbol)
        case (Nil, _) => message
        case (Bit.Zero :: tail, HuffmanBranch(_, _, left, _)) =>
          decodeLoop(tail, left, message)
        case (Bit.One :: tail, HuffmanBranch(_, _, _, right)) =>
          decodeLoop(tail, right, message)

    decodeLoop(bits, tree, Vector.empty)
  end decode

  def encode(
      message: List[Char],
      tree: HuffmanTree
  ): Either[String, List[Bit]] =
    message match
      case Nil => Right(List.empty)
      case head :: tail =>
        for
          encodedHead <- encodeSymbol(head, tree)
          encodedTail <- encode(tail, tree)
        yield encodedHead ++ encodedTail

  def encodeSymbol(symbol: Char, tree: HuffmanTree): Either[String, List[Bit]] =
    tree match
      case leaf: HuffmanLeaf if leaf.symbol == symbol => Right(List.empty)
      case HuffmanBranch(_, _, left, _) if left.symbols.contains(symbol) =>
        encodeSymbol(symbol, left).map: bits =>
          Bit.Zero :: bits
      case HuffmanBranch(_, _, _, right) if right.symbols.contains(symbol) =>
        encodeSymbol(symbol, right).map: bits =>
          Bit.One :: bits
      case _ => Left(s"Symbol $symbol not found in tree")

  def adjoinSet(
      tree: HuffmanTree,
      trees: List[HuffmanTree]
  ): List[HuffmanTree] =
    trees match
      case Nil =>
        List(tree)
      case head :: tail if tree.weight < head.weight =>
        tree :: head :: tail
      case head :: tail =>
        head :: adjoinSet(tree, tail)

  def makeLeafSet(pairs: List[(Char, Int)]): List[HuffmanTree] =
    pairs match
      case Nil => List.empty
      case (symbol, weigth) :: tail =>
        val leaf = HuffmanLeaf(symbol, weigth)
        adjoinSet(leaf, makeLeafSet(tail))

  def generateHuffmanTree(pairs: List[(Char, Int)]): Option[HuffmanTree] =
    successiveMerge(makeLeafSet(pairs))

  def successiveMerge(trees: List[HuffmanTree]): Option[HuffmanTree] =
    trees.sortBy(_.weight) match
      case Nil         => None
      case head :: Nil => Some(head)
      case first :: second :: tail =>
        successiveMerge(makeCodeTree(first, second) :: tail)

end HuffmanTree
