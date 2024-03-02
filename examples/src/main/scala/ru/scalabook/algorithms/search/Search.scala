package ru.scalabook.algorithms.search

import scala.annotation.tailrec
import scala.math.Ordered.orderingToOrdered

object Search:
  @tailrec
  def linearSearch[A](list: List[A], key: A): Option[A] =
    list match
      case Nil                      => None
      case head :: _ if head == key => Some(key)
      case _ :: tail                => linearSearch(tail, key)

  def binarySearch[A: Ordering](list: List[A], key: A): Option[A] =
    def search(l: List[A], r: List[A]): Option[A] =
      if l == r then None
      else test(l, r, middle(l, r))

    def test(l: List[A], r: List[A], m: List[A]): Option[A] =
      if key < m.head then search(l, m)
      else if key > m.head then search(m.tail, r)
      else Some(m.head)

    def middle(l: List[A], r: List[A]): List[A] =
      @tailrec
      def race(t: List[A], h: List[A]): List[A] =
        if h != r && h.tail != r then
          race(t.tail, h.tail.tail)
        else t

      race(l, l.tail)

    search(list, Nil)
  end binarySearch

  def selectionSearch[A: Ordering](list: List[A], n: Int): Option[A] =
    def partitionAndSearch(as: List[A], m: Int): Option[A] = as match
      case Nil    => None
      case h :: t => search(partition(h, t), m)

    def search(t: (List[A], A, List[A]), m: Int): Option[A] =
      if m <= 0 then None
      else
        val (l, p, g) = t
        val len       = l.length
        if len == m - 1 then Some(p)
        else if len >= m then partitionAndSearch(l, m)
        else partitionAndSearch(g, m - len - 1)

    def partition(head: A, tail: List[A]): (List[A], A, List[A]) =
      @tailrec
      def loop(
          rest: List[A],
          less: List[A],
          p: A,
          more: List[A]
      ): (List[A], A, List[A]) = rest match
        case Nil             => (less, p, more)
        case h :: t if h < p => loop(t, h :: less, p, more)
        case h :: t          => loop(t, less, p, h :: more)

      loop(tail, Nil, head, Nil)

    partitionAndSearch(list, n)
  end selectionSearch

  def naiveSubstringSearch(searchWord: String, source: String): Int =
    (0 to (source.length - searchWord.length))
      .find: i =>
        searchWord.indices.forall: j =>
          source(j + i) == searchWord(j)
      .getOrElse(-1)

  def kmpSubstringSearch(searchWord: String, source: String): Int =
    val prefixTab = prefixTable(searchWord)
    source.indices
      .foldLeft((-1, 0)) {
        case ((foundIndex, _), _) if foundIndex > 0 => (foundIndex, 0)
        case ((_, x), i) =>
          val stepsX = LazyList.iterate(x)(x => prefixTab(x - 1))
          val lowerX =
            stepsX
              .find(x => x == 0 || searchWord(x) == source(i))
              .getOrElse(0)
          val newX =
            if searchWord(lowerX) == source(i) then lowerX + 1
            else lowerX
          if newX == searchWord.length then (i - newX + 1, 0)
          else (-1, newX)
      }
      ._1

  private def prefixTable(searchString: String): Vector[Int] =
    searchString.tail
      .foldLeft((0, Vector(0))) {
        case ((initialValue, prefixT), currentCharacter) =>
          val lowerValue =
            LazyList
              .iterate(initialValue)(initialValue => prefixT(initialValue - 1))
              .find(initialValue =>
                initialValue == 0 || searchString(
                  initialValue
                ) == currentCharacter
              )
              .getOrElse(0)
          val newValue =
            if searchString(lowerValue) == currentCharacter then lowerValue + 1
            else lowerValue
          (newValue, prefixT :+ newValue)
      }
      ._2
end Search
