package ru.scalabook.algorithms.trees

import ru.scalabook.algorithms.trees.BinaryTree.*
import ru.scalabook.algorithms.trees.BinarySearchTree.*
import weaver.FunSuite

object BinarySearchTreeSuite extends FunSuite:
  test("BinarySearchTree.searchKey"):
    val myWordList =
      List(("cat", 5), ("dog", 7), ("the", 12), ("for", 4), ("then", 11))
    val myBinSearchTree = myWordList
      .foldLeft(empty[Int]) { case (dict, (key, value)) =>
        dict.insert(key, value)
      }
      .updateValue("then", 16)
    expect.all(
      myBinSearchTree.searchKey("for").contains(4),
      myBinSearchTree.searchKey("then").contains(16),
      myBinSearchTree.searchKey("that").isEmpty
    )

  test("insert and searchKey should work correctly"):
    val dict  = empty[String]
    val dict1 = dict.insert("key1", "value1")
    val dict2 = dict1.insert("key2", "value2")
    val dict3 = dict2.insert("key3", "value3")

    expect.all(
      dict1.searchKey("key1").contains("value1"),
      dict1.searchKey("key").isEmpty,
      dict2.searchKey("key2").contains("value2"),
      dict2.searchKey("key1").contains("value1"),
      dict2.searchKey("key").isEmpty,
      dict3.searchKey("key3").contains("value3"),
      dict3.searchKey("key2").contains("value2"),
      dict3.searchKey("key1").contains("value1"),
      dict3.searchKey("key").isEmpty
    )

  test("updateValue should work correctly"):
    val dict  = empty[Int]
    val dict1 = dict.updateValue("key1", 1)
    val dict2 = dict1.updateValue("key2", 2)
    val dict3 = dict2.updateValue("key3", 3)

    expect.all(
      dict1 == Branch(("key1", 1), Leaf, Leaf),
      dict2 == Branch(("key1", 1), Leaf, Branch(("key2", 2), Leaf, Leaf)),
      dict3 == Branch(
        ("key1", 1),
        Leaf,
        Branch(("key2", 2), Leaf, Branch(("key3", 3), Leaf, Leaf))
      )
    )

  test("remove should work correctly"):
    val dict = empty[Int]
      .insert("key1", 1)
      .insert("key2", 2)
      .insert("key3", 3)

    val dict1 = dict.remove("key2")
    val dict2 = dict1.remove("key3")
    val dict3 = dict2.remove("key1")
    val dict4 = dict3.remove("key4")

    expect.all(
      dict1 == Branch(("key1", 1), Leaf, Branch(("key3", 3), Leaf, Leaf)),
      dict2 == Branch(("key1", 1), Leaf, Leaf),
      dict3 == Leaf,
      dict4 == Leaf
    )
end BinarySearchTreeSuite
