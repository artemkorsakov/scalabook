package ru.scalabook.algorithms.graph

import java.util.UUID

final case class Edge[A](from: UUID, to: UUID, value: A)
