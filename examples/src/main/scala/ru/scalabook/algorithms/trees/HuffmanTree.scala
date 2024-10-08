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
  def makeCodeTree(left: HuffmanTree, right: HuffmanTree) =
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

end HuffmanTree
