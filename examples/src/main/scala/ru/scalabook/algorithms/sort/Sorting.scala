package ru.scalabook.algorithms.sort

import scala.annotation.tailrec
import scala.math.Ordered.orderingToOrdered

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

  def mergeSort[A: Ordering](list: List[A]): List[A] =
    def halveAndSort(as: List[A]) = sort(halve(as))

    def sort(p: (List[A], List[A])): List[A] = p match
      case (Nil, Nil)       => Nil
      case (ha :: Nil, Nil) => ha :: Nil
      case (Nil, hb :: Nil) => hb :: Nil
      case (as, bs)         => merge(halveAndSort(as), halveAndSort(bs))

    def halve(as: List[A]): (List[A], List[A]) =
      @tailrec
      def loop(bs: List[A], fs: List[A], ss: List[A]): (List[A], List[A]) =
        bs match
          case f :: s :: r => loop(r, f :: fs, s :: ss)
          case f :: Nil    => (f :: fs, ss)
          case Nil         => (fs, ss)

      loop(as, Nil, Nil)

    def merge(as: List[A], bs: List[A]): List[A] =
      @tailrec
      def loop(cs: List[A], ds: List[A], r: List[A]): List[A] = (cs, ds) match
        case (Nil, Nil) => r.reverse
        case (_, Nil)   => r.reverse ++ cs
        case (Nil, _)   => r.reverse ++ ds
        case (ha :: ta, hb :: tb) =>
          if ha < hb then loop(ta, ds, ha :: r)
          else loop(cs, tb, hb :: r)

      loop(as, bs, Nil)

    halveAndSort(list)
  end mergeSort

  def quickSort[A: Ordering](list: List[A]): List[A] =
    sealed trait Queue[A]
    case class Element[A](head: A)    extends Queue[A]
    case class Line[A](list: List[A]) extends Queue[A]

    @tailrec
    def partitionAndSort(
        list: List[A],
        sorted: List[A],
        unsorted: List[Queue[A]]
    ): List[A] =
      (list, unsorted) match
        case (Nil, Nil)      => sorted
        case (h :: Nil, Nil) => h :: sorted
        case (Nil, hq :: tq) =>
          hq match
            case Element(el) =>
              partitionAndSort(Nil, el :: sorted, tq)
            case Line(list) =>
              partitionAndSort(list, sorted, tq)
        case (h :: Nil, hq :: tq) =>
          hq match
            case Element(el) =>
              partitionAndSort(Nil, el :: h :: sorted, tq)
            case Line(list) =>
              partitionAndSort(list, h :: sorted, tq)
        case (h :: t, _) =>
          val (l, p, g) = partition(h, t)
          partitionAndSort(l, sorted, Element(p) :: Line(g) :: unsorted)

    def partition(p: A, rest: List[A]): (List[A], A, List[A]) =
      @tailrec
      def loop(
          p: A,
          rest: List[A],
          less: List[A],
          more: List[A]
      ): (List[A], A, List[A]) =
        rest match
          case Nil             => (less, p, more)
          case h :: t if h < p => loop(p, t, h :: less, more)
          case h :: t          => loop(p, t, less, h :: more)

      loop(p, rest, Nil, Nil)

    partitionAndSort(list, Nil, Nil).reverse
  end quickSort

end Sorting
