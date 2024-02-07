package ru.scalabook.algorithms.games.nim

/** <a href="https://en.wikipedia.org/wiki/Nim">Nim</a> */
object Nim:
  def getX(head: Long, params: Long*): Long =
    params.indices.foldLeft(head)((res, i) => res ^ params(i))
