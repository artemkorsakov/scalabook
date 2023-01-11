package algorithms.matrix

/** Orthogonal matrix m * n. */
class OrthogonalMatrix[T](elements: Seq[Seq[T]])(using I: Integral[T])
    extends SquaredMatrix[T](elements):
  require(isOrthogonalMatrix)
