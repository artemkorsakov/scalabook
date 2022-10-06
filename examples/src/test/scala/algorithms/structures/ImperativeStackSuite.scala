package algorithms.structures

import munit.FunSuite

class ImperativeStackSuite extends FunSuite:
  test("ImperativeStack.push") {
    val stack = new ImperativeStack[Int](3)
    stack.push(1)
    stack.push(2)
    stack.push(3)
    interceptMessage[java.lang.IllegalArgumentException]("Can't push to the full stack") {
      stack.push(4)
    }
  }

  test("ImperativeStack.push to the full stack") {
    val stackWithZeroSize = new ImperativeStack[Int](0)
    interceptMessage[java.lang.IllegalArgumentException]("Can't push to the full stack") {
      stackWithZeroSize.push(1)
    }
  }

  test("ImperativeStack.isEmpty") {
    val stack = new ImperativeStack[Int](2)
    assert(stack.isEmpty)
    stack.push(1)
    assert(!stack.isEmpty)
    stack.push(2)
    assert(!stack.isEmpty)
  }

  test("ImperativeStack.isFull") {
    val stack = new ImperativeStack[Int](2)
    assert(!stack.isFull)
    stack.push(1)
    assert(!stack.isFull)
    stack.push(2)
    assert(stack.isFull)
  }


  /*
  test("ImperativeStack.pop") {
    assertEquals(binaryTree.preorder, Vector(5, 4, 1, 0, 2, 3, 6))
  }

  test("ImperativeStack.peek") {
    assertEquals(binaryTree.preorder, Vector(5, 4, 1, 0, 2, 3, 6))
  }

  test("ImperativeStack.isEmpty") {
    assertEquals(binaryTree.preorder, Vector(5, 4, 1, 0, 2, 3, 6))
  }

  test("ImperativeStack.isFull") {
    assertEquals(binaryTree.preorder, Vector(5, 4, 1, 0, 2, 3, 6))
  }
   */
