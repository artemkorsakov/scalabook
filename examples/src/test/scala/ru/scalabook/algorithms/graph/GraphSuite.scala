package ru.scalabook.algorithms.graph

import weaver.*
import weaver.scalacheck.*

object GraphSuite extends SimpleIOSuite with Checkers:
  private val emptyGraph = Graph.empty[String, Int]

  pureTest("addVertex should add a new vertex to the graph"):
    val newGraph = emptyGraph.addVertex("test")
    expect.all(
      newGraph.vertices.size == 1,
      newGraph.vertices.exists(_._2.value == "test")
    )

  pureTest("addVertex should not add a duplicate vertex to the graph"):
    val newGraph = emptyGraph.addVertex("test").addVertex("test")
    expect.all(
      newGraph.vertices.size == 1,
      newGraph.vertices.exists(_._2.value == "test")
    )

  test("addVertex should add multiple different vertices to the graph"):
    forall: (values: List[String]) =>
      val newGraph =
        values.foldLeft(emptyGraph)((g, value) => g.addVertex(value))
      expect.same(newGraph.vertices.size, values.distinct.size)

  pureTest("addEdge should add a new edge to the graph"):
    val newGraph =
      emptyGraph
        .addVertex("from")
        .addVertex("to")
        .addEdge("from", "to", 1)
    whenSuccess(newGraph): graph =>
      expect(graph.edges.size == 1) and {
        val edge      = graph.edges.head
        val maybeFrom = graph.vertices.get(edge.from)
        val maybeTo   = graph.vertices.get(edge.to)
        expect.all(
          maybeFrom.exists(_.value == "from"),
          maybeTo.exists(_.value == "to"),
          edge.value == 1
        )
      }

  pureTest(
    "addEdge should return an error if the source vertex does not exist"
  ):
    val graph  = Graph.empty[String, String].addVertex("A")
    val result = graph.addEdge("B", "C", "edge")

    matches(result) { case Left(error) =>
      expect(error == "Исходящая вершина не найдена")
    }

  pureTest(
    "addEdge should return an error if the destination vertex does not exist"
  ):
    val graph  = Graph.empty[String, String].addVertex("A")
    val result = graph.addEdge("A", "B", "edge")

    matches(result) { case Left(error) =>
      expect(error == "Конечная вершина не найдена")
    }

  pureTest("addEdge should return an error if the edge already exists"):
    val maybeGraph =
      Graph.empty[String, String]
        .addVertex("A")
        .addVertex("B")
        .addEdge("A", "B", "edge")

    whenSuccess(maybeGraph): graph =>
      val result = graph.addEdge("A", "B", "edge")

      matches(result) { case Left(error) =>
        expect(error == "Эта вершина уже содержится в графе")
      }

  pureTest(
    "addEdge should return an error if the edge with different direction already exists"
  ):
    val maybeGraph =
      Graph.empty[String, String]
        .addVertex("A")
        .addVertex("B")
        .addEdge("A", "B", "edge")

    whenSuccess(maybeGraph): graph =>
      val result = graph.addEdge("B", "A", "edge")

      matches(result) { case Left(error) =>
        expect(error == "Эта вершина уже содержится в графе")
      }

  pureTest("addEdge should add multiple edges to the graph"):
    val graph =
      Graph.empty[String, String]
        .addVertex("A")
        .addVertex("B")
        .addVertex("C")

    val result1 = graph.addEdge("A", "B", "edge1")
    val result2 = result1.flatMap(_.addEdge("B", "C", "edge2"))
    val result3 = result2.flatMap(_.addEdge("A", "C", "edge3"))

    expect(result1.isRight) and
      expect(result2.isRight) and
      expect(result3.isRight)

end GraphSuite
