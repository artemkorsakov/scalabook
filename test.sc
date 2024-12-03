object Nim:
  def getX(head: Long, params: Long*): Long =
    params.indices.foldLeft(head)((res, i) => res ^ params(i))

println(Nim.getX(1, 2, 3))
