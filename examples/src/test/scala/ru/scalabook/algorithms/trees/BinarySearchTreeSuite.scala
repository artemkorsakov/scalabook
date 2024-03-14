package ru.scalabook.algorithms.trees

import ru.scalabook.algorithms.trees.BinarySearchTree.*
import ru.scalabook.algorithms.trees.BinaryTree.*
import weaver.FunSuite

object BinarySearchTreeSuite extends FunSuite:
  test("insert into empty dictionary") {
    val dict        = empty[Int]
    val updatedDict = dict.insert("key", 1)
    expect.same(updatedDict, Branch(("key", 1), Leaf, Leaf))
  }

  test("insert into non-empty dictionary with key less than root") {
    val dict        = empty[Int].insert("key1", 1)
    val updatedDict = dict.insert("key0", 2)
    expect.same(
      updatedDict,
      Branch(("key1", 1), Branch(("key0", 2), Leaf, Leaf), Leaf)
    )
  }

  test("insert into non-empty dictionary with key greater than root") {
    val dict        = empty[Int].insert("key1", 1)
    val updatedDict = dict.insert("key2", 2)
    expect.same(
      updatedDict,
      Branch(("key1", 1), Leaf, Branch(("key2", 2), Leaf, Leaf))
    )
  }

  test("insert into non-empty dictionary with key equal to root") {
    val dict        = empty[Int].insert("key1", 1)
    val updatedDict = dict.insert("key1", 2)
    expect.same(updatedDict, Branch(("key1", 1), Leaf, Leaf))
  }

  test("insert multiple elements into dictionary") {
    val dict = empty[Int]
    val updatedDict = dict
      .insert("key1", 1)
      .insert("key2", 2)
      .insert("key3", 3)
      .insert("key4", 4)

    val expectedDict = Branch(
      ("key1", 1),
      Leaf,
      Branch(
        ("key2", 2),
        Leaf,
        Branch(("key3", 3), Leaf, Branch(("key4", 4), Leaf, Leaf))
      )
    )

    expect.same(updatedDict, expectedDict)
  }

  test("search in empty dictionary") {
    val dict   = empty[Int]
    val result = dict.searchKey("key")
    expect.same(result, None)
  }

  test("search for existing key in dictionary") {
    val dict   = empty[Int].insert("key", 1)
    val result = dict.searchKey("key")
    expect.same(result, Some(1))
  }

  test("search for non-existing key in dictionary") {
    val dict   = empty[Int].insert("key", 1)
    val result = dict.searchKey("non-existing")
    expect.same(result, None)
  }

  test("search for key in right subtree") {
    val dict   = empty[Int].insert("key1", 1).insert("key2", 2)
    val result = dict.searchKey("key2")
    expect.same(result, Some(2))
  }

  test("search for key in left subtree") {
    val dict   = empty[Int].insert("key1", 1).insert("key0", 2)
    val result = dict.searchKey("key0")
    expect.same(result, Some(2))
  }

  test("update value in empty dictionary") {
    val dict        = empty[Int]
    val updatedDict = dict.updateValue("key", 1)
    expect.same(updatedDict, Branch(("key", 1), Leaf, Leaf))
  }

  test("update value in dictionary with existing key") {
    val dict        = empty[Int].insert("key", 1)
    val updatedDict = dict.updateValue("key", 2)
    expect.same(updatedDict, Branch(("key", 2), Leaf, Leaf))
  }

  test("update value in dictionary with non-existing key") {
    val dict        = empty[Int].insert("key1", 1)
    val updatedDict = dict.updateValue("key2", 2)
    expect.same(
      updatedDict,
      Branch(("key1", 1), Leaf, Branch(("key2", 2), Leaf, Leaf))
    )
  }

  test("update value in dictionary with key in left subtree") {
    val dict        = empty[Int].insert("key1", 1).insert("key0", 2)
    val updatedDict = dict.updateValue("key0", 3)
    expect.same(
      updatedDict,
      Branch(("key1", 1), Branch(("key0", 3), Leaf, Leaf), Leaf)
    )
  }

  test("update value in dictionary with key in right subtree") {
    val dict        = empty[Int].insert("key1", 1).insert("key2", 2)
    val updatedDict = dict.updateValue("key2", 3)
    expect.same(
      updatedDict,
      Branch(("key1", 1), Leaf, Branch(("key2", 3), Leaf, Leaf))
    )
  }

  test("remove from empty dictionary") {
    val dict        = empty[Int]
    val updatedDict = dict.remove("key")
    expect.same(updatedDict, Leaf)
  }

  test("remove non-existing key from dictionary") {
    val dict        = empty[Int].insert("key", 1)
    val updatedDict = dict.remove("non-existing")
    expect.same(updatedDict, dict)
  }

  test("remove existing key from dictionary") {
    val dict        = empty[Int].insert("key1", 1).insert("key0", 2)
    val updatedDict = dict.remove("key1")
    expect.same(updatedDict, Branch(("key0", 2), Leaf, Leaf))
  }

  test("remove key from dictionary with left subtree") {
    val dict        = empty[Int].insert("key1", 1).insert("key0", 2)
    val updatedDict = dict.remove("key0")
    expect.same(updatedDict, Branch(("key1", 1), Leaf, Leaf))
  }

  test("remove key from dictionary with right subtree") {
    val dict        = empty[Int].insert("key1", 1).insert("key2", 2)
    val updatedDict = dict.remove("key2")
    expect.same(updatedDict, Branch(("key1", 1), Leaf, Leaf))
  }

  test("remove key from dictionary with equal left and right subtrees") {
    val dict = empty[Int].insert("key1", 1).insert("key0", 0).insert("key2", 2)
    val updatedDict = dict.remove("key2")
    expect.same(
      updatedDict,
      Branch(("key1", 1), Branch(("key0", 0), Leaf, Leaf), Leaf)
    )
  }

  test("size of empty dictionary") {
    val dict = empty[Int]
    expect(dict.size == 0)
  }

  test("size of non-empty dictionary") {
    val dict = empty[Int].insert("key", 1)
    expect(dict.size == 1)
  }

  test("isEmpty for empty dictionary") {
    val dict = empty[Int]
    expect(dict.isEmpty)
  }

  test("isEmpty for non-empty dictionary") {
    val dict = empty[Int].insert("key", 1)
    expect(!dict.isEmpty)
  }

  test("isValid for empty dictionary") {
    val dict = empty[Int]
    expect(dict.isValid)
  }

  test("isValid for dictionary with single element") {
    val dict = empty[Int].insert("key", 1)
    expect(dict.isValid)
  }

  test("isValid for dictionary with valid left subtree") {
    val dict = empty[Int].insert("key1", 1).insert("key0", 0)
    expect(dict.isValid)
  }

  test("isValid for dictionary with valid right subtree") {
    val dict = empty[Int].insert("key1", 1).insert("key2", 2)
    expect(dict.isValid)
  }

  test("isValid for dictionary with valid subtrees") {
    val dict = empty[Int].insert("key1", 1).insert("key0", 0).insert("key2", 2)
    expect(dict.isValid)
  }

  test("isBalanced for empty dictionary") {
    val dict = empty[Int]
    expect(dict.isBalanced)
  }

  test("isBalanced for dictionary with single element") {
    val dict = empty[Int].insert("key", 1)
    expect(dict.isBalanced)
  }

  test("isBalanced for balanced dictionary with the same height") {
    val dict = empty[Int].insert("key1", 1).insert("key0", 0).insert("key2", 2)
    expect(dict.isBalanced)
  }

  test("isBalanced for balanced dictionary") {
    val dict = empty[Int].insert("key1", 1).insert("key0", 0).insert(
      "key2",
      2
    ).insert("key3", 3)
    expect(dict.isBalanced)
  }

  test("isBalanced for unbalanced dictionary") {
    val dict = empty[Int].insert("key1", 1).insert("key0", 0).insert(
      "key2",
      2
    ).insert("key3", 3).insert("key4", 4)
    expect(!dict.isBalanced)
  }
end BinarySearchTreeSuite
