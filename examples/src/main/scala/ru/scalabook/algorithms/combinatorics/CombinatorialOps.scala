package ru.scalabook.algorithms.combinatorics

/** A tiny class that extends a list with four combinatorial operations:
  * ''combinations'', ''subsets'', ''permutations'', ''variations''.
  *
  * You can find all the ideas behind this code at blog-post:
  *
  * https://kostyukov.net/posts/combinatorial-algorithms-in-scala/
  *
  * How to use this class.
  *
  * import CombinatorialOps.*
  *
  * val v = List(1, 2, 3).xvariations(2)
  *
  * The following functions are available:
  *
  *   - xcombinations(n) -- generates n-combinations
  *   - xsubsets -- generates all subsets
  *   - xvariations(n) -- generates n-variations
  *   - xpermutations -- generates all permutations
  *
  * @see
  *   <a
  *   href="https://kostyukov.net/posts/combinatorial-algorithms-in-scala/">Kostyukov.net:
  *   Combinatorial Algorithms in Scala</a>
  */
object CombinatorialOps:

  extension [A](list: List[A])

    /** Generates the combinations of this list with given length 'n'. The order
      * doesn't matter.
      *
      * The total number of k-combinations on n-length set might be calculated
      * as follows:
      *
      * C_k,n = n!/k!(n - k)!
      *
      * Time - O(C_k,n) Space - O(C_k,n)
      */
    def xcombinations(n: Int): List[List[A]] =
      val l = list.length
      if n > l then Nil
      else if n == l then List(list)
      else
        list match
          case Nil         => Nil
          case _ if n == 1 => list.map(List(_))
          case hd :: tl    =>
            tl.xcombinations(n - 1).map(hd :: _) ::: tl.xcombinations(n)

    /** Generates all the subsets of this list. The order doesn't matter.
      *
      * The total number of subsets might be obtained from variations formula:
      *
      * S_n = sum(i=1..n) {C_i,n} = 2 ** n Time - O(S_n) Space - O(S_n)
      */
    def xsubsets
        : List[List[A]] = (2 to list.length).foldLeft(list.xcombinations(1)):
      (a, i) =>
        list.xcombinations(i) ::: a

    /** Generates the variations of this list with given length 'n'. The order
      * does matter.
      *
      * The total number of variations might be calculated as follows:
      *
      * V_k,n = n!/(n - k)!
      *
      * Time - O(V_k,n) Space - O(V_k,n)
      */
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
          case hd :: tl    =>
            mixmany(hd, tl.xvariations(n - 1)) ::: tl.xvariations(n)
    end xvariations

    /** Generates all permutations of this list. The order does matter.
      *
      * The total number of permutations might be calculated as follows:
      *
      * P_n = V_n,n = n!
      *
      * Time - O(n!) Space - O(n!)
      */
    def xpermutations: List[List[A]] = xvariations(list.length)
  end extension
end CombinatorialOps
