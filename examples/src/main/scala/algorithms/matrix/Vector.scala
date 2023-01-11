package algorithms.matrix

case class Vector[N](row: Seq[N])(using I: Integral[N]):
  require(row.nonEmpty)

  lazy val norm: Double = math.sqrt(I.toDouble(*(row)))

  def +(col: Seq[N]): Seq[N] =
    require(row.length == col.length)
    row.indices.map(i => I.plus(row(i), col(i)))

  def *(a: N): Seq[N] = row.map(I.times(_, a))

  def *(col: Seq[N]): N =
    require(row.length == col.length)
    row.indices.foldLeft(I.zero)((s, i) => I.plus(s, I.times(row(i), col(i))))

  def *(col: Seq[N], module: N): N =
    require(row.length == col.length)
    row.indices.foldLeft(I.zero)((s, i) =>
      posMod(I.plus(s, I.times(row(i), col(i))), module)
    )

  def *(matrix: Matrix[N]): Seq[N] =
    rowToMatrix.*(matrix).elements.head

  def *(matrix: Matrix[N], module: N): Seq[N] =
    rowToMatrix.*(matrix, module).elements.head

  def distance(col: Seq[N]): Double =
    val d = row.indices.foldLeft(I.zero) { (s, i) =>
      val d = I.minus(row(i), col(i))
      I.plus(s, I.times(d, d))
    }
    math.sqrt(I.toDouble(d))

  def cos(col: Seq[N]): Double =
    val scal = *(col)
    val a    = norm
    val b    = Vector(col).norm
    scal.toString.toDouble / (a * b)

  def isOrthogonal(col: Seq[N]): Boolean = *(col) == I.zero

  def manhattanMetric(col: Seq[N]): N =
    require(row.length == col.length)
    row.indices.foldLeft(I.zero)((s, i) =>
      I.plus(s, I.abs(I.minus(row(i), col(i))))
    )

  def rowToMatrix: Matrix[N] = new Matrix[N](Seq(row))

  def columnToMatrix: Matrix[N] = new Matrix[N](row.map(Seq(_)))

  private def posMod(x: N, y: N): N = I.rem(I.plus(I.rem(x, y), y), y)

end Vector
