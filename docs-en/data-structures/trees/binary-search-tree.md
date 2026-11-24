# Binary Search Tree

**Definition:**

A binary tree is a [binary search tree](https://en.wikipedia.org/wiki/Binary_search_tree)
if the value at the root node is greater than or equal to
all values in the left subtree and less than or equal to all values in the right subtree.
This applies to all nodes, guaranteeing that elements are in ascending order.

**Complexity:**

- _Search for an element_ (`search`)
    - **Time** - O(log n)
    - **Space** - O(log n)
- _Insertion of an element_ (`insert`)
    - **Time** - O(log n)
    - **Space** - O(log n)
- _Updating the value of an element_ (`update`)
    - **Time** - O(log n)
    - **Space** - O(log n) 
- _Removal of an element_ (`remove`)
    - **Time** - O(log n)
    - **Space** - O(log n)
  
**Code:**

The following code defines methods for working with a binary search tree.

The main public methods defined as `extension` for `Dictionary[A]` include:

1. `insert(key: String, value: A): Dictionary[A]` - Inserts a new key-value pair into the tree.

2. `searchKey(key: String): Option[A]` - Searches for a value by key. If the key is found, returns `Some(value)`, otherwise `None`.

3. `updateValue(key: String, value: A): Dictionary[A]` - Updates the value by key.
   If the key is not found, it inserts a new key-value pair.

4. `remove(key: String): Dictionary[A]` - Removes a key-value pair by key.
   If the key is not found, the tree remains unchanged.

```dotty
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
```

## Binary tree methods

**Code:**

The following code has several methods that are "inherited" from [binary tree].

```dotty
object BinarySearchTree:  
  extension [A](dict: Dictionary[A])    
    def isEmpty: Boolean = dict.isEmpty

    def size: Int = dict.size

    def height: Int = dict.height
```

## Search tree parameters

**Code:**

The following code has several methods that work with binary trees and dictionaries based on these trees.

- `isValid` - a method that checks whether the `Dictionary` is a valid binary search tree.
  It checks that for each node all keys in the left subtree are less than the node's key,
  and all keys in the right subtree are greater than the node's key.
- `isBalanced` - this method checks whether the `Dictionary` is a balanced binary tree.
  It checks that for each node the difference in heights of the left and right subtrees is at most 1,
  and recursively checks the left and right subtrees.

```dotty
object BinarySearchTree:  
  extension [A](dict: Dictionary[A])    
    def isValid: Boolean = dict match
      case Leaf                       => true
      case Branch((_, _), Leaf, Leaf) => true
      case Branch((key, _), Leaf, rb @ Branch((keyR, _), _, _)) =>
        key <= keyR && rb.isValid
      case Branch((key, _), lb @ Branch((keyL, _), _, _), Leaf) =>
        key >= keyL && lb.isValid
      case Branch(
            (key, _),
            lb @ Branch((keyL, _), _, _),
            rb @ Branch((keyR, _), _, _)
          ) =>
        key >= keyL && key <= keyR && lb.isValid && rb.isValid

    def isBalanced: Boolean = dict match
      case Leaf => true
      case Branch((_, _), lb, rb) =>
        math.abs(lb.height - rb.height) <= 1 && lb.isBalanced && rb.isBalanced
  end extension
end BinarySearchTree
```

---

**Links:**

- [Source code][source]
- [Donald E. Knuth - The Art of Computer Programming, section 2.3.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Алгоритмика - Деревья поиска](https://ru.algorithmica.org/cs/tree-structures/)

[source]: https://gitverse.ru/scalabook/algorithms
