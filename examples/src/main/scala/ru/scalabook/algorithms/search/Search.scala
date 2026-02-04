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
    @tailrec
    def search(lst: List[A], sub: List[A]): Option[A] =
      if lst == sub then None
      else
        middle(lst, sub) match
          case Nil          => None
          case head :: next =>
            if key < head then search(lst, head :: next)
            else if key > head then search(next, sub)
            else Some(head)

    def middle(lst: List[A], sub: List[A]): List[A] =
      @tailrec
      def race(oneStep: List[A], twoSteps: List[A]): List[A] =
        if twoSteps.nonEmpty && twoSteps != sub && twoSteps.tail != sub then
          race(oneStep.drop(1), twoSteps.drop(2))
        else oneStep

      race(lst, lst)

    search(list, Nil)
  end binarySearch

  def selectionSearch[A: Ordering](list: List[A], n: Int): Option[A] =
    @tailrec
    def partitionAndSearch(as: List[A], m: Int): Option[A] = as match
      case Nil    => None
      case h :: t =>
        val (l, p, g) = partition(h, t)
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
      ): (List[A], A, List[A]) =
        rest match
          case Nil             => (less, p, more)
          case h :: t if h < p => loop(t, h :: less, p, more)
          case h :: t          => loop(t, less, p, h :: more)

      loop(tail, Nil, head, Nil)

    if n <= 0 then None
    else partitionAndSearch(list, n)
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
        case ((_, x), i)                            =>
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
