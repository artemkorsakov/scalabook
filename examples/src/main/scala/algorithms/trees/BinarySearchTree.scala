package algorithms.trees

import algorithms.trees.BinaryTree.*

object BinarySearchTree:
  opaque type Dictionary[A] = BinaryTree[(String, A)]

  def empty[A]: Dictionary[A] = Leaf

  extension [A](dict: Dictionary[A])
    def insert(key: String, value: A): Dictionary[A] =
      dict match
        case Leaf =>
          Branch((key, value), Leaf, Leaf)
        case Branch((k, v), lb, rb) if key < k =>
          Branch((k, v), lb.insert(key, value), rb)
        case Branch((k, v), lb, rb) if key > k =>
          Branch((k, v), lb, rb.insert(key, value))
        case Branch((_, _), _, _) => dict

    def searchKey(key: String): Option[A] =
      dict match
        case Leaf                             => None
        case Branch((k, _), lb, _) if key < k => lb.searchKey(key)
        case Branch((k, _), _, rb) if key > k => rb.searchKey(key)
        case Branch((_, v), _, _)             => Some(v)

    def updateValue(key: String, value: A): Dictionary[A] =
      dict match
        case Leaf => Branch((key, value), Leaf, Leaf)
        case Branch((k, _), lb, rb) if key < k =>
          Branch((k, value), lb.updateValue(key, value), rb)
        case Branch((k, _), lb, rb) if key > k =>
          Branch((k, value), lb, rb.updateValue(key, value))
        case Branch((_, _), lb, rb) =>
          Branch((key, value), lb, rb)
