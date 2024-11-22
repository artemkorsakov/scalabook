extension [A](list: List[A])
  def xvariations(n: Int): List[List[A]] =
    def mixmany(a: A, lists: List[List[A]]): List[List[A]] =
      lists match
        case Nil      => Nil
        case hd :: tl => foldone(a, hd) ::: mixmany(a, tl)

    def foldone(a: A, lists: List[A]): List[List[A]] =
      (1 to lists.length).foldLeft(List(a :: lists)): (acc, i) =>
        mixone(i, a, lists) :: acc

    def mixone(i: Int, a: A, lists: List[A]): List[A] =
      lists.slice(0, i) ::: (a :: lists.slice(i, lists.length))

    if n > list.length then Nil
    else
      list match
        case Nil         => Nil
        case _ if n == 1 => list.map(List(_))
        case hd :: tl =>
          mixmany(hd, tl.xvariations(n - 1)) ::: tl.xvariations(n)
  end xvariations

  def xpermutations: List[List[A]] = xvariations(list.length)

end extension

println(List("a", "b", "c").xvariations(2))
println(List("a", "b", "c").xpermutations)
