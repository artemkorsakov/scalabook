package ru.scalabook.algorithms.trees

import ru.scalabook.algorithms.trees.BinaryTree.*

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
        case _ => dict

    def searchKey(key: String): Option[A] =
      dict match
        case Leaf                             => None
        case Branch((k, _), lb, _) if key < k => lb.searchKey(key)
        case Branch((k, _), _, rb) if key > k => rb.searchKey(key)
        case Branch((_, v), _, _)             => Some(v)

    def updateValue(key: String, value: A): Dictionary[A] =
      dict match
        case Leaf => Branch((key, value), Leaf, Leaf)
        case Branch((k, v), lb, rb) if key < k =>
          Branch((k, v), lb.updateValue(key, value), rb)
        case Branch((k, v), lb, rb) if key > k =>
          Branch((k, v), lb, rb.updateValue(key, value))
        case Branch((_, _), lb, rb) =>
          Branch((key, value), lb, rb)

    def remove(key: String): Dictionary[A] =
      dict match
        case Leaf => Leaf
        case Branch((k, v), lb, rb) if key < k =>
          Branch((k, v), lb.remove(key), rb)
        case Branch((k, v), lb, rb) if key > k =>
          Branch((k, v), lb, rb.remove(key))
        case Branch((_, _), lb, rb) =>
          if lb.size >= rb.size then
            lb.popMax.map { case (item, dict) =>
              Branch(item, dict, rb)
            }.getOrElse(Leaf)
          else
            rb.popMin.map { case (item, dict) =>
              Branch(item, lb, dict)
            }.getOrElse(Leaf)

    private def popMax: Option[((String, A), Dictionary[A])] = dict match
      case Leaf                     => None
      case Branch((k, v), lb, Leaf) => Some(((k, v), lb))
      case Branch((k, v), lb, rb) =>
        rb.popMax.map { case (item, dict) =>
          (item, Branch((k, v), lb, dict))
        }

    private def popMin: Option[((String, A), Dictionary[A])] = dict match
      case Leaf                     => None
      case Branch((k, v), Leaf, rb) => Some(((k, v), rb))
      case Branch((k, v), lb, rb) =>
        lb.popMin.map { case (item, dict) =>
          (item, Branch((k, v), dict, rb))
        }
  end extension
end BinarySearchTree
