package ru.scalabook.algorithms.graph

import java.util.UUID

final case class Vertex[A: Ordering](id: UUID, value: A):
  def contains(value: A): Boolean = summon[Ordering[A]].equiv(this.value, value)
