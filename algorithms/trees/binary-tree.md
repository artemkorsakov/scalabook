# Двоичное дерево

[Двоичное дерево (бинарное дерево)](https://ru.wikipedia.org/wiki/%D0%94%D0%B2%D0%BE%D0%B8%D1%87%D0%BD%D0%BE%D0%B5_%D0%B4%D0%B5%D1%80%D0%B5%D0%B2%D0%BE) — 
это особая форма дерева, которое имеет не более двух дочерних элементов. 
Информацию, представленную в виде бинарных деревьев, гораздо удобнее обрабатывать, 
так как бинарные деревья обладают определенными свойствами. 

### Структура

Формально бинарное дерево может быть представлено тройкой `T = (x, L, R)`, 
где `x` представляет узел, а `L` и `R` - левую и правую ветви соответственно. 
`L` и `R` являются непересекающимися бинарными деревьями и не содержат `x`. 
Термин «двоичный» происходит от двух дочерних элементов, 
т.е. каждый узел в двоичном дереве может иметь не более двух дочерних элементов. 
Также это называется степенью бинарного дерева, которая равна 2. 

Чтобы бинарное дерево было полным (полное бинарное дерево), оно должно удовлетворять двум условиям:

1. Все листья находятся на одном уровне и 
2. У каждого внутреннего узла есть два потомка

Несколько важных определений, связанных со структурой бинарных деревьев или деревьев в целом:

- путь (_path_): определяется как последовательность узлов (x<sub>0</sub>, x<sub>1</sub>, x<sub>2</sub>, ..., x<sub>n</sub>), 
  где узлы с соседними нижними индексами являются соседними узлами. 
  Поскольку деревья ацикличны, путь не может содержать один и тот же узел более одного раза. 
- длина пути (_path length_): определяется как количество `n` соседних пар. 
- корневой путь (_root path_): для узла x<sub>0</sub> его корневой путь определяется как путь (x<sub>0</sub>, x<sub>1</sub>, x<sub>2</sub>, ..., x<sub>n</sub>), где x<sub>n</sub> — корень дерева. 
- глубина (_depth_): определяется как длина корневого пути
- высота (_height_): это определяется как наибольшая глубина среди всех его узлов 
- уровень (_level_): это набор всех узлов на заданной глубине
- размер (_size_): определяется как количество неконечных узлов

### Возможная реализация в Scala

```scala
enum BinaryTree[+A]:
  case Leaf
  case Branch(value: A, left: BinaryTree[A], right: BinaryTree[A])

  lazy val size: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + l.size + r.size

  lazy val depth: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + l.depth.max(r.depth)

object BinaryTree:
  def apply[A](list: List[A]): BinaryTree[A] =
    list match
      case Nil => Leaf
      case x :: xs =>
        val (leftList, rightList) = xs.splitAt(xs.length / 2)
        Branch(x, BinaryTree(leftList), BinaryTree(rightList))
```

### Алгоритмы обхода дерева

###### Preorder

1. Посетить корень
2. Выполнить preorder обход левого поддерева, если оно не пустое
3. Выполнить preorder обход правого поддерева, если оно не пустое

###### Inorder

1. Выполнить inorder обход левого поддерева, если оно не пустое
2. Посетить корень
3. Произвести inorder обход правого поддерева, если оно не пустое

###### Postorder

1. Выполнить postorder обход левого поддерева, если оно не пустое
2. Выполнить postorder обход правого поддерева, если оно не пустое
3. Посетить корень

```scala
def preorder[A](binTree: BinaryTree[A]): IndexedSeq[A] =
  binTree match
    case Leaf                       => IndexedSeq.empty
    case Branch(value, left, right) => IndexedSeq(value) ++ preorder(left) ++ preorder(right)

def inorder[A](binTree: BinaryTree[A]): IndexedSeq[A] =
  binTree match
    case Leaf                                   => IndexedSeq.empty
    case Branch(value, leftBranch, rightBranch) => inorder(leftBranch) ++ IndexedSeq(value) ++ inorder(rightBranch)

def postorder[A](binTree: BinaryTree[A]): IndexedSeq[A] =
  binTree match
    case Leaf => IndexedSeq.empty
    case Branch(value, leftBranch, rightBranch) =>
      postorder(leftBranch) ++ postorder(rightBranch) ++ IndexedSeq(value)
```


[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Ftrees%2FBinaryTree.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Ftrees%2FBinaryTreeSuite.scala)


---

**References:**
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)