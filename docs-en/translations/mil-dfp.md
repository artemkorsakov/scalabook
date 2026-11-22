---
layout: default
title: Millionaire's DFP
parent: Translations
summary: Deriving functionally pure implementations of common data structures.
nav_order: 4
---

# Millionaire's DFP
{: .no_toc }

{% include toc.html %}

# Millionaire's DFP

Here we discuss functional programming techniques for implementing commonly used data
structures in a functional, pure way.

## Lists

The linked list is a classic data structure. In Scala:

```scala
sealed trait List[+A]
case object Nil extends List[Nothing]
case class Cons[A](head: A, tail: List[A]) extends List[A]
```

Common operations:

```scala
def map[A, B](as: List[A])(f: A => B): List[B] = as match
  case Nil => Nil
  case Cons(h, t) => Cons(f(h), map(t)(f))

def filter[A](as: List[A])(p: A => Boolean): List[A] = as match
  case Nil => Nil
  case Cons(h, t) => if p(h) then Cons(h, filter(t)(p)) else filter(t)(p)

def flatten[A](lists: List[List[A]]): List[A] = lists match
  case Nil => Nil
  case Cons(h, t) => append(h, flatten(t))
```

## Trees

A binary tree:

```scala
sealed trait Tree[+A]
case class Leaf[A](value: A) extends Tree[A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
```

Operations:

```scala
def size[A](t: Tree[A]): Int = t match
  case Leaf(_) => 1
  case Branch(l, r) => size(l) + size(r)

def maximum(t: Tree[Int]): Int = t match
  case Leaf(n) => n
  case Branch(l, r) => math.max(maximum(l), maximum(r))

def depth[A](t: Tree[A]): Int = t match
  case Leaf(_) => 0
  case Branch(l, r) => 1 + math.max(depth(l), depth(r))

def map[A, B](t: Tree[A])(f: A => B): Tree[B] = t match
  case Leaf(a) => Leaf(f(a))
  case Branch(l, r) => Branch(map(l)(f), map(r)(f))
```

## Sets

```scala
sealed trait Set[A]:
  def contains(x: A): Boolean
  def +(x: A): Set[A]
  def -(x: A): Set[A]
  def ∪(other: Set[A]): Set[A]
  def ∩(other: Set[A]): Set[A]
  def ⊆(other: Set[A]): Boolean
  def foldLeft[B](z: B)(f: (B, A) => B): B
```

## Graphs

```scala
case class Graph[A](nodes: Set[A], edges: Set[(A, A)])
```

## Queues

```scala
case class Queue[A](leading: List[A], trailing: List[A]):
  def head = if leading.isEmpty then throw new NoSuchElementException else leading.head
  def tail: Queue[A] =
    val rest = leading.tail
    if rest.isEmpty then Queue(trailing.reverse, Nil)
    else Queue(rest, trailing)
  def isEmpty = leading.isEmpty
  def +[B >: A](x: B) = Queue(leading, x :: trailing)
```

## Further Reading

- *Functional Data Structures* by Chris Okasaki
- [Purely Functional Data Structures](https://www.amazon.com/Purely-Functional-Structures-Chris-Okasaki/dp/0521663504) by Chris Okasaki
