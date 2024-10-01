def elementOfSet[A: Ordering](set: List[A], a: A): Boolean =
  val ord = summon[Ordering[A]]
  set match
    case Nil                       => false
    case h :: _ if ord.equiv(h, a) => true
    case h :: _ if ord.lt(a, h)    => false
    case _ :: t                    => elementOfSet(t, a)

println("elementOfSet")
println(elementOfSet(List(1, 2, 3, 4, 5), 3)) // true
println(elementOfSet(List(1, 3, 4, 5, 6), 2)) // false

def intersectionSet[A: Ordering](set1: List[A], set2: List[A]): List[A] =
  val ord = summon[Ordering[A]]
  (set1, set2) match
    case (Nil, _) => Nil
    case (_, Nil) => Nil
    case (head1 :: tail1, head2 :: tail2) if ord.equiv(head1, head2) =>
      head1 :: intersectionSet(tail1, tail2)
    case (head1 :: tail1, head2 :: _) if ord.lt(head1, head2) =>
      intersectionSet(tail1, set2)
    case (_, _ :: tail2) =>
      intersectionSet(set1, tail2)

println("intersectionSet")
println(intersectionSet(List(1, 2, 3, 4, 5), List(4, 5, 6, 7, 8)))
// List(4, 5)
println(intersectionSet(List(1, 3, 5, 7, 9), List(2, 4, 6, 8, 10)))
// List()
println(intersectionSet(List(1, 2, 3, 4, 5), List(1, 2, 3, 4, 5)))
// List(1, 2, 3, 4, 5)

def adjoinSet[A: Ordering](set: List[A], a: A): List[A] =
  val ord = summon[Ordering[A]]
  set match
    case Nil                       => a :: Nil
    case h :: _ if ord.equiv(h, a) => set
    case h :: _ if ord.lt(a, h)    => a :: set
    case h :: t                    => h :: adjoinSet(t, a)

println("adjoinSet")
println(adjoinSet(List(1, 2, 3, 4, 5), 3))
// List(1, 2, 3, 4, 5)
println(adjoinSet(List(1, 3, 4, 5, 6), 2))
// List(1, 2, 3, 4, 5, 6)
println(adjoinSet(List(1, 2, 3, 4, 5), 6))
// List(1, 2, 3, 4, 5, 6)

def unionSet[A: Ordering](set1: List[A], set2: List[A]): List[A] =
  val ord = summon[Ordering[A]]
  (set1, set2) match
    case (Nil, _) => set2
    case (_, Nil) => set1
    case (head1 :: tail1, head2 :: tail2) if ord.equiv(head1, head2) =>
      head1 :: unionSet(tail1, tail2)
    case (head1 :: tail1, head2 :: _) if ord.lt(head1, head2) =>
      head1 :: unionSet(tail1, set2)
    case (_, head2 :: tail2) =>
      head2 :: unionSet(set1, tail2)

println("unionSet")
println(unionSet(List(1, 2, 3, 4, 5), List(4, 5, 6, 7, 8)))
// List(1, 2, 3, 4, 5, 6, 7, 8)
println(unionSet(List(1, 3, 5, 7, 9), List(2, 4, 6, 8, 10)))
// List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
println(unionSet(List(1, 2, 3, 4, 5), List(1, 2, 3, 4, 5)))
// List(1, 2, 3, 4, 5)
