package algorithms.structures

import munit.FunSuite

class ImperativeStackSuite extends FunSuite:
  test("ImperativeStack.push") {
    val stack = new ImperativeStack[Int](2)
    stack.push(1)
    stack.push(2)
    interceptMessage[java.lang.IllegalArgumentException]("Can't add element to full stack") {
      stack.push(3)
    }
    val stackWithZeroSize = new ImperativeStack[Int](0)
    interceptMessage[java.lang.IllegalArgumentException]("Can't add element to full stack") {
      stackWithZeroSize.push(1)
    }
  }

  test("ImperativeStack.pop") {
    val stack = new ImperativeStack[Int](2)
    interceptMessage[java.lang.IllegalArgumentException]("Can't get element to empty stack") {
      stack.pop()
    }
    stack.push(1)
    assertEquals(stack.pop(), 1)
    assert(stack.isEmpty)
    stack.push(1)
    stack.push(2)
    assert(stack.isFull)
    assertEquals(stack.pop(), 2)
    stack.push(3)
    assert(stack.isFull)
  }

  test("ImperativeStack.peek") {
    val stack = new ImperativeStack[Int](2)
    interceptMessage[java.lang.IllegalArgumentException]("Can't get element to empty stack") {
      stack.peek()
    }
    stack.push(1)
    assertEquals(stack.peek(), 1)
    stack.push(2)
    assertEquals(stack.peek(), 2)
  }

  test("ImperativeStack.isEmpty") {
    val stack = new ImperativeStack[Int](2)
    assert(stack.isEmpty)
    stack.push(1)
    assert(!stack.isEmpty)
    stack.push(2)
    assert(!stack.isEmpty)
    val stackWithZeroSize = new ImperativeStack[Int](0)
    assert(stackWithZeroSize.isEmpty)
  }

  test("ImperativeStack.isFull") {
    val stack = new ImperativeStack[Int](2)
    assert(!stack.isFull)
    stack.push(1)
    assert(!stack.isFull)
    stack.push(2)
    assert(stack.isFull)
    val stackWithZeroSize = new ImperativeStack[Int](0)
    assert(stackWithZeroSize.isFull)
  }
