package algorithms.others

object OtherAlgorithms:
  def factorial(n: Int): BigInt =
    (2 to n).foldLeft(BigInt(1))((f, i) => f * BigInt(i))

  def functionAckermann(x: Int, y: Int): Int =
    if y == 0 then 0
    else if x == 0 then 2 * y
    else if y == 1 then 2
    else functionAckermann(x - 1, functionAckermann(x, y - 1))
