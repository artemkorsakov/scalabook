# Graphs

A _graph_ is typically defined as a set of points (called _vertices_)
together with a set of lines (called _edges_) that connect certain pairs of vertices.
Each pair of vertices is connected by at most one edge.
Two vertices are called _adjacent_ if there is an edge between them.
If \\(V\\) and \\(\acute{V}\\) are vertices and \\(n \geq 0\\), then \\((V\_{0}, V\_{1}, ... , V\_{n})\\)
is called a _path_ of length n from vertex \\(V\\) to vertex \\(\acute{V}\\),
if \\(V = V\_{0}\\), vertex \\(V\_{k}\\) is adjacent to vertex \\(V\_{k+1}\\) for \\(0 \leq k < n\\),
and \\(V\_{n} = \acute{V}\\).
A path is called _simple_ if vertices \\(V\_{0}, V\_{1}, ... , V\_{n−1}\\) are distinct
and if vertices \\(V\_{1}, ... , V\_{n−1}, V\_{n}\\) are distinct.
A graph is called _connected_ if there exists a path between any two of its vertices.
A _cycle_ is a simple path of length greater than or equal to three, from some vertex to itself.

**Code:**

The following code defines classes `Vertex`, `Edge` and `Graph` that represent an undirected graph.

- The `addVertex` method adds a new vertex to the graph. If a vertex with the same value already exists,
  it returns the current graph. If no vertex with that value exists, it creates a new vertex and adds it to the graph.
- The `addEdge` method adds a new edge to the graph. It checks whether the source and target vertices exist in the graph.
  If either vertex is not found, it returns the corresponding error.
  If both vertices are found, it checks whether an edge already exists between these vertices.
  If the edge already exists, it returns the corresponding error.
  If the edge does not exist, it creates a new edge and adds it to the graph.
- The `areAdjacent` method checks whether two vertices are adjacent (i.e., whether there is an edge between them).
  If either vertex is not found, it returns the corresponding error.
  If both vertices are found, it checks whether an edge exists between these vertices and returns the result.
- The methods `findVertexes`, `findVertex`, `findEdge` are used to search for vertices and edges in the graph.

```dotty
final case class Vertex[A: Ordering](id: UUID, value: A):
  def contains(value: A): Boolean = summon[Ordering[A]].equiv(this.value, value)

final case class Edge[A](from: UUID, to: UUID, value: A)

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
      (fromVertex, toVertex) <- findVertexes(from, to)
      edge <- if findEdge(fromVertex.id, toVertex.id).isDefined then
        Left("This vertex is already in the graph")
      else Right(Edge(fromVertex.id, toVertex.id, value))
    yield Graph(vertices, edges :+ edge)

  def areAdjacent(from: A, to: A): Either[String, Boolean] =
    findVertexes(from, to).map(vs => findEdge(vs._1.id, vs._2.id).isDefined)

  private def findVertexes(
      from: A,
      to: A
  ): Either[String, (Vertex[A], Vertex[A])] =
    for
      fromVertex <- findVertex(from).toRight("Source vertex not found")
      toVertex   <- findVertex(to).toRight("Target vertex not found")
    yield (fromVertex, toVertex)

  private def findVertex(value: A): Option[Vertex[A]] =
    vertices.collectFirst {
      case (id, vertex) if vertex.contains(value) => vertex
    }

  private def findEdge(from: UUID, to: UUID): Option[Edge[B]] =
    edges.find: edge =>
      edge.from == from && edge.to == to ||
        edge.from == to && edge.to == from

end Graph
```

## Free Tree

A _free tree_, or unrooted tree, is defined as a connected graph without cycles.

##### Theorem

If G is a graph, the following statements are equivalent for it.

- a) G is a free tree.
- b) G is a connected graph that becomes disconnected upon removal of any arbitrary edge.
- c) If \\(V\\) and \\(\acute{V}\\) are distinct vertices of graph G,
  then there exists a unique simple path from vertex \\(V\\) to vertex \\(\acute{V}\\).
  Furthermore, if graph G is finite and contains exactly n > 0 vertices,
  the following statements are also equivalent to (a)–(c).
- d) G contains no cycles and has n − 1 edges.
- e) G is a connected graph that has n − 1 edges.

## Directed Graph

Formally, we define a _directed graph_ (or _digraph_) as a set of vertices and a set of _arcs_ or _edges_,
each of which extends from vertex \\(V\\) to vertex \\(\acute{V}\\).
If e is an arc from vertex \\(V\\) to vertex \\(\acute{V}\\), we call V the _initial_ vertex of arc e,
and \\(\acute{V}\\) the _final_ vertex, and write \\(V = init(e)\\), \\(\acute{V} = fin(e)\\).
In this case, it is possible that \\(init(e) = fin(e)\\) (though in the definition of an edge of an ordinary graph this is excluded)
and several distinct arcs may have the same initial and final vertices.
The _out-degree_ of vertex \\(V\\) is the number of arcs emanating from it,
namely, the number of arcs e such that \\(init(e) = V\\).
Similarly, the _in-degree_ of vertex \\(V\\) is defined as the number of arcs for which \\(fin(e) = V\\).

If \\(e\_{1}, e\_{2}, ... , e\_{n}\\) are arcs (with \\(n \geq 1\\)), then we consider
(\\(e\_{1}, e\_{2}, ... , e\_{n}\\)) to be an _oriented path_ of length n
from vertex \\(V\\) to vertex \\(\acute{V}\\), if \\(V = init(e\_{1}), \acute{V} = fin(e\_{n})\\),
and \\(fin(e\_{k}) = init(e\_{k+1})\\) for \\(1 \leq k < n\\).
An oriented path (\\(e\_{1}, e\_{2}, ... , e\_{n}\\)) is called _simple_
if \\(init(e\_{1}), ... , init(e\_{n})\\) are distinct and \\(fin(e\_{1}), ... , fin(e\_{n})\\) are distinct.
An _oriented cycle_ is a simple oriented path from some vertex to itself.

A directed graph is called _strongly connected_
if there exists an oriented path from vertex \\(V\\) to vertex \\(\acute{V}\\) for any two vertices \\(V \neq \acute{V}\\).
It is _rooted_ if there exists at least one _root_,
i.e. at least one vertex \\(R\\)
such that given \\(R\\), there exists an oriented path from \\(V\\) to \\(R\\) for all \\(V \neq R\\).
A "strongly connected" graph is always "rooted", but the converse is not true.

## Oriented Tree

An _oriented tree_, sometimes called a rooted tree,
is a directed graph with such a vertex \\(R\\) that:

- a) each vertex \\(V \neq R\\) is the initial vertex of exactly one arc, denoted as \\(e\[V\]\\);
- b) \\(R\\) is not the initial vertex of any arc;
- c) \\(R\\) is a root in the sense specified above
  (i.e. for each vertex \\(V \neq R\\) there exists an oriented path from \\(V\\) to \\(R\\)).

## Balanced Directed Graph

An _Eulerian circuit_ in a directed graph is such an oriented path \\((e\_{1}, e\_{2}, ... , e\_{m})\\),
that each arc of the directed graph appears in this path exactly once and \\(fin(e\_{m}) = init(e\_{1})\\).
It represents a "complete traversal" of the arcs of the digraph.

A directed graph is called _balanced_
if each vertex V has equal in-degree and out-degree,
i.e. as many edges exist for which vertex V is initial
as many edges exist for which vertex V is final.
If a directed graph has an Eulerian circuit, then obviously it must be connected and balanced,
except in cases where it has _isolated vertices_,
i.e. vertices with in-degree and out-degree equal to zero.

## Theorems

### Hierholzer's Theorem

> A finite directed graph without isolated vertices contains an Eulerian circuit if and only if
> it is connected and balanced.

---

**Links:**

- [Source code][source]
- [Donald E. Knuth - The Art of Computer Programming, section 2.3.4.1 - 2.3.4.2](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[source]: https://gitverse.ru/scalabook/algorithms
