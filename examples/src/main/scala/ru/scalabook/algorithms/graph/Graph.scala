package ru.scalabook.algorithms.graph

import java.util.UUID

final case class Graph[A: Ordering, B] private (
    vertices: Map[UUID, Vertex[A]],
    edges: Vector[Edge[B]]
):
  self =>

  def addVertex(value: A): Graph[A, B] =
    if vertices.exists(_._2.contains(value)) then self
    else
      val newVertex = Vertex(UUID.randomUUID(), value)
      Graph(vertices + (newVertex.id -> newVertex), edges)

  def addEdge(from: A, to: A, value: B): Either[String, Graph[A, B]] =
    for
      fromVertex <- findVertex(from).toRight("Исходящая вершина не найдена")
      toVertex   <- findVertex(to).toRight("Конечная вершина не найдена")
      edge <- if findEdge(fromVertex.id, toVertex.id).isDefined then
        Left("Эта вершина уже содержится в графе")
      else Right(Edge(fromVertex.id, toVertex.id, value))
    yield Graph(vertices, edges :+ edge)

  private def findVertex(value: A): Option[Vertex[A]] =
    vertices.collectFirst {
      case (id, vertex) if vertex.contains(value) => vertex
    }

  private def findEdge(from: UUID, to: UUID): Option[Edge[B]] =
    edges.find: edge =>
      edge.from == from && edge.to == to ||
        edge.from == to && edge.to == from

end Graph

object Graph:
  def empty[A: Ordering, B]: Graph[A, B] = Graph(Map.empty, Vector.empty)
