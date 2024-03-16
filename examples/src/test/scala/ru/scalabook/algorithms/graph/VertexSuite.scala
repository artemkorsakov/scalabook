package ru.scalabook.algorithms.graph

import weaver.*
import weaver.scalacheck.*

import java.util.UUID

object VertexSuite extends SimpleIOSuite with Checkers:
  test("Vertex contains method should return true when the value is equal"):
    forall: (value: String) =>
      val vertex = Vertex(UUID.randomUUID(), value)
      expect(vertex.contains(value))

  test(
    "Vertex contains method should return false when the value is not equal"
  ):
    forall: (value: String, notValue: String) =>
      val vertex = Vertex(UUID.randomUUID(), value)
      expect(value == notValue || !vertex.contains(notValue))
end VertexSuite
