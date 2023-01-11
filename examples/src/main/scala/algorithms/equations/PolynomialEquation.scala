package algorithms.equations

/** <a href="https://en.wikipedia.org/wiki/Polynomial">Polynomial equation</a>.
  */
object PolynomialEquation:

  /** Solution of a polynomial equation with given coefficients for a given n.
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def polynomialSolution(n: Int, aList: Array[Long]): Long =
    var number = 1L
    aList.reverse.foldLeft(0L) { (sum, a) =>
      val s = sum + a * number
      number *= n
      s
    }

  /** Returns the coefficients of a polynomial equation.
    *
    * @param k
    *   \- the power of equation + 1: a[1]*n<sup>k-1</sup> + ... +
    *   a[i]*n<sup>k-i</sup> + ... + a[k]
    * @param solutions
    *   \- known solutions for 1, 2, 3, ... , k
    * @return
    *   a[1], a[2], ..., a[k]
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def polynomialCoefficients(k: Int, solutions: Array[Long]): Array[Long] =
    val aList = new Array[Long](k)

    val coefficientsList = coefficientsForSolutionSearching(k)
    for (i <- k - 1 to 0 by -1) do
      val coefficients = coefficientsList(i)
      val index        = k - coefficients.length
      var divisor      = 0L
      for (j <- coefficients.indices) do
        aList(index) += coefficients(j) * solutions(coefficients.length - 1 - j)
        (0 until index).foreach(m =>
          aList(index) -= aList(m) * coefficients(j) * math
            .pow(coefficients.length.toDouble - j, k - 1.0 - m)
            .toLong
        )
        divisor += coefficients(j) * math
          .pow(
            coefficients.length.toDouble - j,
            coefficients.length.toDouble - 1
          )
          .toLong
      aList(index) = aList(index) / divisor

    aList

  private def coefficientsForSolutionSearching(
      k: Int
  ): IndexedSeq[IndexedSeq[Long]] =
    if k == 1 then IndexedSeq(IndexedSeq(1L))
    else
      val coefficients = coefficientsForSolutionSearching(k - 1)
      val last         = coefficients.last
      val current      = 1L +: (0 until last.length - 1).map(i =>
        last(i + 1) - last(i)
      ) :+ -last.last
      coefficients :+ current
