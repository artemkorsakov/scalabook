package ru.scalabook.algorithms.combinatorics

object Permutation:
  /** Построение перестановки длины n из перестановки длины n - 1. Просто
    * поочередно добавляем новый элемент на все возможные места в исходной
    * перестановке.
    */
  def constructByMethod1[A](source: Vector[A], a: A): Vector[Vector[A]] =
    (0 to source.length).map { i =>
      val (before, after) = source.splitAt(i)
      before ++ Vector(a) ++ after
    }.toVector

  def constructByMethod2(source: Vector[Int]): Vector[Vector[Int]] =
    (1 to source.length + 1).map { k =>
      source.map { v => if v >= k then v + 1 else v } :+ k
    }.toVector
