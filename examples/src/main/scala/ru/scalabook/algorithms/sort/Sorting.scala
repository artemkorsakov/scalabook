package ru.scalabook.algorithms.sort

import scala.annotation.tailrec
import scala.math.Ordered.orderingToOrdered
import scala.reflect.ClassTag

object Sorting:
  def bubbleSort[A: Ordering](list: List[A]): List[A] =
    @tailrec
    def loop(as: List[A], zs: List[A], sorted: List[A]): List[A] =
      as match
        case Nil       => sorted
        case h1 :: Nil => loop(zs, Nil, h1 :: sorted)
        case h1 :: h2 :: t =>
          if h1 > h2 then loop(h1 :: t, h2 :: zs, sorted)
          else loop(h2 :: t, h1 :: zs, sorted)

    loop(list, Nil, Nil)

  def selectionSort[A: Ordering](list: List[A]): List[A] =
    @tailrec
    def loop(max: A, tail: List[A], zs: List[A], sorted: List[A]): List[A] =
      (tail, zs) match
        case (Nil, Nil)             => max :: sorted
        case (Nil, h :: t)          => loop(h, t, Nil, max :: sorted)
        case (h :: t, _) if max > h => loop(max, t, h :: zs, sorted)
        case (h :: t, _)            => loop(h, t, max :: zs, sorted)

    list match
      case Nil          => Nil
      case head :: tail => loop(head, tail, Nil, Nil)

  def insertionSort[A: Ordering](list: List[A]): List[A] =
    @tailrec
    def sort(as: List[A], bs: List[A]): List[A] = as match
      case Nil    => bs
      case h :: t => sort(t, insert(h, bs))

    def insert(a: A, as: List[A]): List[A] = as match
      case h :: t if a > h => h :: insert(a, t)
      case _               => a :: as

    sort(list, Nil)

  def mergeSort[T: ClassTag: Ordering](array: Array[T]): Unit =
    mergeSort(array, 0, array.length - 1)

  def quickSort[T: Ordering](list: List[T]): List[T] =
    list match
      case Nil      => list
      case _ :: Nil => list
      case h :: tail =>
        val (p1, p2)     = tail.partition(el => summon[Ordering[T]].lteq(el, h))
        val leftToPivot  = quickSort(p1)
        val rightToPivot = quickSort(p2)
        leftToPivot ++ (h :: rightToPivot)

  private def mergeSort[T: ClassTag: Ordering](
      array: Array[T],
      first: Int,
      last: Int
  ): Unit =
    if last <= first then ()
    else
      val mid = first + (last - first) / 2
      mergeSort(array, first, mid)
      mergeSort(array, mid + 1, last)
      mergeParts(array, first, last, mid)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private def mergeParts[T: ClassTag: Ordering](
      array: Array[T],
      first: Int,
      last: Int,
      mid: Int
  ): Unit =
    val buf = new Array[T](array.length)
    val _   = array.copyToArray(buf)

    var i = first
    var j = mid + 1
    for k <- first to last do
      if i > mid then
        array(k) = buf(j)
        j += 1
      else if j > last then
        array(k) = buf(i)
        i += 1
      else if summon[Ordering[T]].lt(buf(j), buf(i)) then
        array(k) = buf(j)
        j += 1
      else
        array(k) = buf(i)
        i += 1
  end mergeParts

end Sorting
