package algorithms.fundamental

object Numerical:
  /** Преобразование десятичного числа в двоичное */
  def decToBinConv(x: Int): String =
    val seqOfDivByTwo = Iterator.iterate(x)(a => a / 2)
    val binList = seqOfDivByTwo
      .takeWhile(a => a > 0)
      .map(a => a % 2)
    binList.mkString.reverse
