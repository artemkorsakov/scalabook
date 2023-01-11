package algorithms.matrix

import algorithms.matrix.Vector

/** Matrix m * n.
  *
  * @see
  *   <a href="https://en.wikipedia.org/wiki/Matrix_(mathematics)">detailed
  *   description</a>
  * @see
  *   <a href="http://vmath.ru/vf5/algebra2">detailed description</a>
  */
case class Matrix[T](elements: Seq[Seq[T]])(using I: Integral[T]):

  /** Row's count. */
  val m: Int = elements.length
  require(m > 0)

  /** Column's count. */
  val n: Int = elements.head.length
  require((0 until m).forall(i => elements(i).length == n))

  def row(i: Int): Seq[T] = elements(i)

  def column(j: Int): Seq[T] = (0 until m).map(i => elements(i)(j))

  lazy val rows: Seq[Seq[T]] = elements

  lazy val columns: Seq[Seq[T]] =
    (0 until n).map(j => (0 until m).map(i => elements(i)(j)))

  lazy val diagonals: Seq[Seq[T]] =
    (0 until n).map(j => (0 until (n - j)).map(i => elements(i)(j + i))) ++
      (1 until m).map(i => (0 until (m - i)).map(j => elements(i + j)(j)))

  lazy val oppDiagonals: Seq[Seq[T]] =
    (0 until n).map(j => (0 to j).map(i => elements(i)(j - i))) ++
      (1 until m).map(i =>
        ((n - 1) to (i + n - m) by -1).map(j => elements(i + n - 1 - j)(j))
      )

  lazy val mainDiagonal: Seq[T] =
    (0 until math.min(n, m)).map(i => elements(i)(i))

  lazy val isSquared: Boolean = m == n

  lazy val topLeft: T = elements.head.head

  /** <a href="https://en.wikipedia.org/wiki/Transpose">Transpose</a> of a
    * matrix.
    */
  def transpose: Matrix[T] =
    Matrix((0 until n).map(j => (0 until m).map(i => elements(i)(j))))

  /** New matrix without the given row and the given column. */
  def minor(row: Int, column: Int): Matrix[T] =
    Matrix(
      (0 until m)
        .withFilter(_ != row)
        .map(i => (0 until n).withFilter(_ != column).map(j => elements(i)(j)))
    )

  def isTheSameSize(other: Matrix[T]): Boolean = m == other.m && n == other.n

  def +(other: Matrix[T]): Matrix[T] =
    require(isTheSameSize(other))
    Matrix(
      (0 until m).map(i =>
        (0 until n).map(j => I.plus(elements(i)(j), other.elements(i)(j)))
      )
    )

  def *(c: T): Matrix[T] =
    Matrix(
      (0 until m).map(i => (0 until n).map(j => I.times(elements(i)(j), c)))
    )

  def *(c: T, module: T): Matrix[T] =
    Matrix(
      (0 until m).map(i =>
        (0 until n).map(j => posMod(I.times(elements(i)(j), c), module))
      )
    )

  /** <a
    * href="https://en.wikipedia.org/wiki/Matrix_multiplication">multiplication</a>
    */
  def *(other: Matrix[T]): Matrix[T] =
    require(n == other.m)
    val newElements = (0 until m)
      .map(i =>
        (0 until other.n).map { j =>
          Vector(elements(i)).*((0 until n).map(k => other.elements(k)(j)))
        }
      )
    Matrix(newElements)

  def *(other: Matrix[T], module: T): Matrix[T] =
    require(n == other.m)
    val newElements = (0 until m)
      .map(i =>
        (0 until other.n).map(j =>
          Vector(elements(i))
            .*((0 until n).map(k => other.elements(k)(j)), module)
        )
      )
    Matrix(newElements)

  def *(other: Vector[T]): Vector[T] =
    Vector(*(other.columnToMatrix).elements.map(_.head))

  def *(other: Vector[T], module: T): Vector[T] =
    Vector(*(other.columnToMatrix, module).elements.map(_.head))

  /** Matrix exponentiation. */
  def power(p: Long): Matrix[T] =
    require(p >= 1)
    if p == 1 then this
    else
      val powers  = p.toBinaryString
      val powersC = new Array[Matrix[T]](powers.length)
      powersC(0) = this
      (1 until powers.length).foreach(i =>
        powersC(i) = powersC(i - 1) * powersC(i - 1)
      )
      var result  = powersC.last
      (1 until powers.length).withFilter(powers(_) == '1').foreach { i =>
        result = result * powersC(powersC.length - 1 - i)
      }
      result

  def power(p: Long, module: T): Matrix[T] =
    require(p >= 1)
    if p == 1 then Matrix(elements.map(_.map(posMod(_, module))))
    else
      val powers  = p.toBinaryString
      val powersC = new Array[Matrix[T]](powers.length)
      powersC(0) = this
      (1 until powers.length).foreach(i =>
        powersC(i) = powersC(i - 1) * (powersC(i - 1), module)
      )
      var result  = powersC.last
      (1 until powers.length).withFilter(powers(_) == '1').foreach { i =>
        result = result * (powersC(powersC.length - 1 - i), module)
      }
      result

  // http://vmath.ru/vf5/algebra2#konkatenacija
  def concatenation(other: Matrix[T]): Matrix[T] =
    require(m == other.m)
    Matrix((0 until m).map(i => elements(i) ++ other.elements(i)))

  // https://vmath.ru/vf5/algebra2#vektorizacija
  def vectorization: Seq[T] =
    (0 until n).foldLeft(Seq.empty[T])((seq, i) => seq ++ column(i))

  lazy val toSquaredMatrix: SquaredMatrix[T] = SquaredMatrix[T](elements)

  lazy val toOrthogonalMatrix: OrthogonalMatrix[T] =
    OrthogonalMatrix[T](elements)

  override def toString: String =
    elements.map(row => row.mkString("| ", ", ", " |")).mkString("\n")

  override def hashCode: Int = elements.hashCode()

  private def posMod(x: T, y: T): T = I.rem(I.plus(I.rem(x, y), y), y)

end Matrix
