package algorithms.structures

import scala.reflect.ClassTag

class ImperativeStack[A: ClassTag](maxSize: Int):
  private val stackBox = new Array[A](maxSize)
  private var top = -1

  def push(data: A): Unit =
    if isFull then throw new IllegalArgumentException("Can't push to the full stack")
    else
      top += 1
      stackBox(top) = data

  def pop(): A =
    val popData = peek()
    top -= 1
    popData

  def peek(): A =
    stackBox(top)

  def isEmpty: Boolean =
    top == -1

  def isFull: Boolean =
    top == maxSize - 1