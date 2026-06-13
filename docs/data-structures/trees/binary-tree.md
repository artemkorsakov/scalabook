{%
showPrev = true
prevPage = ../trees.html
showNext = true
nextPage = binary-search-tree.html
%}

# Двоичное дерево

[Двоичное дерево (бинарное дерево)](https://ru.wikipedia.org/wiki/%D0%94%D0%B2%D0%BE%D0%B8%D1%87%D0%BD%D0%BE%D0%B5_%D0%B4%D0%B5%D1%80%D0%B5%D0%B2%D0%BE) — 
это особая форма дерева, которое имеет не более двух дочерних элементов. 
Информацию, представленную в виде двоичных деревьев, гораздо удобнее обрабатывать, 
так как двоичные деревья обладают определенными свойствами. 

## Структура

Формально двоичное дерево может быть представлено тройкой `T = (x, L, R)`, 
где `x` представляет узел, а `L` и `R` - левую и правую ветви соответственно. 
`L` и `R` являются непересекающимися двоичными деревьями и не содержат `x`. 
Термин «двоичный» происходит от двух дочерних элементов, 
т.е. каждый узел в двоичном дереве может иметь не более двух дочерних элементов. 
Также это называется степенью двоичного дерева, которая равна 2. 

Чтобы двоичное дерево было полным (полное двоичное дерево), оно должно удовлетворять двум условиям:

1. Все листья находятся на одном уровне и 
2. У каждого внутреннего узла есть два потомка

Несколько важных определений, связанных со структурой двоичных деревьев или деревьев в целом:

- путь (_path_): определяется как последовательность узлов (\\(x\_{0}, x\_{1}, x\_{2}, ... , x\_{n}\\)), 
  где узлы с соседними нижними индексами являются соседними узлами. 
  Поскольку деревья ацикличны, путь не может содержать один и тот же узел более одного раза. 
- длина пути (_path length_): определяется как количество \\(n\\) соседних пар. 
- корневой путь (_root path_): для узла \\(x\_{0}\\) его корневой путь определяется как путь (\\(x\_{0}, x\_{1}, x\_{2}, ... , x\_{n}\\)), где \\(x\_{n}\\) — корень дерева. 
- глубина (_depth_): определяется как длина корневого пути
- высота (_height_): определяется как наибольшая глубина среди всех его узлов 
- уровень (_level_): это набор всех узлов на заданной глубине
- размер (_size_): определяется как количество неконечных узлов

**Код:**

```dotty
enum BinaryTree[+A]:
  case Leaf
  case Branch(value: A, left: BinaryTree[A], right: BinaryTree[A])

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false
    
  lazy val size: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + l.size + r.size

  lazy val height: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + math.max(l.height, r.height)
    
  def rootPath[B >: A](b: B): Option[NonEmptyList[B]] = this match
    case Leaf                              => None
    case Branch(value, _, _) if value == b => Some(NonEmptyList.one(b))
    case Branch(value, left, right) =>
      left.rootPath(b).orElse(right.rootPath(b)).map: nel =>
        value :: nel
  
  def level(level: Int): List[A] =
    @tailrec
    def loop(level: Int, trees: List[BinaryTree[A]]): List[A] =
      if level < 0 then List.empty
      else if level == 0 then
        trees.foldLeft(List.empty[A]) { case (acc, tree) =>
          tree match
            case Leaf            => acc
            case Branch(v, _, _) => v :: acc
        }
      else
        loop(
          level - 1,
          trees.flatMap:
            case Branch(_, left, right) if !left.isEmpty && !right.isEmpty =>
              List(left, right)
            case Branch(_, left, _) if !left.isEmpty   => List(left)
            case Branch(_, _, right) if !right.isEmpty => List(right)
            case _ => List.empty[BinaryTree[A]]
        )

    loop(level, List(this))
  end level      
```

**Пример:**

```dotty
val exampleBinaryTree: BinaryTree[Char] = Branch(
  'A',
  Branch('B', Branch('D', Leaf, Leaf), Leaf),
  Branch(
    'C',
    Branch('E', Leaf, Branch('G', Leaf, Leaf)),
    Branch('F', Branch('H', Leaf, Leaf), Branch('J', Leaf, Leaf))
  )
)

exampleBinaryTree.size          // 9
exampleBinaryTree.height        // 4
exampleBinaryTree.rootPath('G') // Some(NonEmptyList(A, C, E, G))
exampleBinaryTree.level(3)      // List(J, H, G)
```

## Построение дерева

**Алгоритм:**

В объекте `BinaryTree` определены методы `apply`, 
которые создают бинарные деревья из различных типов коллекций данных:

- Метод `apply` с одним аргументом возвращает узел дерева с этим значением и двумя пустыми поддеревьями.
- Метод `apply`, принимающий коллекцию, строит бинарное дерево из заданных значений. 
  Если коллекция пуста, то возвращается пустой лист. 
  В противном случае, коллекция без первого элемента делится пополам, 
  создается узел с первым элементом и двумя поддеревьями, созданными из левой и правой разбитой части.

**Код:**

```dotty
object BinaryTree:
  def apply[A](a: A): BinaryTree[A] = Branch(a, Leaf, Leaf)

  def apply[A](values: List[A]): BinaryTree[A] =
    values match
      case Nil => Leaf
      case x :: xs =>
        val (left, right) = xs.splitAt(xs.length / 2)
        Branch(x, BinaryTree(left), BinaryTree(right))

  def apply[A](values: Vector[A]): BinaryTree[A] =
    if values.isEmpty then Leaf
    else
      val (left, right) = values.tail.splitAt(values.length / 2)
      Branch(values.head, BinaryTree(left), BinaryTree(right))
```

## Алгоритмы обхода дерева

Существует достаточно много алгоритмов работы с древовидными структурами, 
в которых наиболее часто встречается понятие _обхода_ (_traversing_) дерева или _"прохода"_ по дереву. 
При таком методе исследования дерева каждый узел посещается в точности один раз, 
а полный обход дерева задает линейное упорядочение узлов, что позволяет упростить алгоритм, 
так как при этом можно использовать понятие "следующий" узел, 
т.е. узел, который располагается перед данным узлом в таком упорядочении или после него.

Для обхода бинарного дерева можно применить один из трех принципиально разных способов: 
_в прямом порядке_ (_preorder_), _в центрированном порядке_ (_inorder_) или _в обратном порядке_ (_postorder_).

### Preorder (прямой порядок)

1. Посетить корень
2. Выполнить preorder обход левого поддерева, если оно не пустое
3. Выполнить preorder обход правого поддерева, если оно не пустое

### Inorder (центрированный порядок)

1. Выполнить inorder обход левого поддерева, если оно не пустое
2. Посетить корень
3. Произвести inorder обход правого поддерева, если оно не пустое

### Postorder (обратный порядок)

1. Выполнить postorder обход левого поддерева, если оно не пустое
2. Выполнить postorder обход правого поддерева, если оно не пустое
3. Посетить корень

**Код:**

```dotty
lazy val preorder: IndexedSeq[A] = this match
  case Leaf                       => IndexedSeq.empty
  case Branch(value, left, right) => IndexedSeq(value) ++ left.preorder ++ right.preorder

lazy val inorder: IndexedSeq[A] = this match
  case Leaf                       => IndexedSeq.empty
  case Branch(value, left, right) => left.inorder ++ IndexedSeq(value) ++ right.inorder

lazy val postorder: IndexedSeq[A] = this match
  case Leaf                       => IndexedSeq.empty
  case Branch(value, left, right) => left.postorder ++ right.postorder ++ IndexedSeq(value)
```

**Пример:**

Если применить эти определения к бинарному дереву 
`('A', ('B', ('D', -, -), -), ('C', ('E', -, ('G', -, -)), ('F', ('H', -, -), ('J', -, -))))`, 
то при прямом порядке обхода узлов получим последовательность `A B D C E G F H J`.
(Сначала следует корень `A`, затем — левое поддерево в прямом порядке, и, наконец, правое поддерево в прямом порядке.)

При центрированном порядке обхода корень посещается после обхода узлов одного из деревьев точно так, 
как если бы узлы дерева "проектировались" на горизонтальную прямую с образованием последовательности `D B A E G C H F J`.

Аналогично обратный порядок обхода позволяет получить последовательность `D B G E H J F C A`.

```dotty
val binaryTree: BinaryTree[Int] = Branch(
  'A',
  Branch('B', Branch('D', Leaf, Leaf), Leaf),
  Branch(
    'C',
    Branch('E', Leaf, Branch('G', Leaf, Leaf)),
    Branch('F', Branch('H', Leaf, Leaf), Branch('J', Leaf, Leaf))
  )
)

binaryTree.preorder
// Vector('A', 'B', 'D', 'C', 'E', 'G', 'F', 'H', 'J')

binaryTree.inorder
// Vector('D', 'B', 'A', 'E', 'G', 'C', 'H', 'F', 'J')

binaryTree.postorder
// Vector('D', 'B', 'G', 'E', 'H', 'J', 'F', 'C', 'A')
```

## Подобие и эквивалентность

Пусть \\(u\_{1}, u\_{2}, ... , u\_{n}\\) и \\(\acute{u}\_{1}, \acute{u}\_{2}, ... , \acute{u}\_{\acute{n}}\\) 
являются узлами бинарных деревьев \\(T\\) и \\(\acute{T}\\) соответственно в прямом порядке обхода.

Для любого узла u положим

- \\(l(u)=1\\), если \\(u\\) имеет непустое левое поддерево, иначе \\(l(u) = 0\\);
- \\(r(u)=1\\), если \\(u\\) имеет непустое правое поддерево, иначе \\(r(u) = 0\\);

\\(T\\) и \\(\acute{T}\\) **подобны** тогда и только тогда, когда \\(n = \acute{n}\\) и 
\\(l(u\_{j}) = l(\acute{u}\_{j})\\), \\(r(u\_{j}) = r(\acute{u}\_{j})\\) для \\(1 \leq j \leq n\\).

Более того, подобные деревья \\(T\\) и \\(\acute{T}\\) **эквивалентны** тогда и только тогда, когда 
\\(info(u\_{j}) = info(\acute{u}\_{j})\\) для \\(1 \leq j \leq n\\).

**Код:**

```dotty
def areSimilar[A, B](treeA: BinaryTree[A], treeB: BinaryTree[B]): Boolean =
  (treeA, treeB) match
    case (Leaf, Leaf) => true
    case (Branch(_, leftA, rightA), Branch(_, leftB, rightB)) =>
      areSimilar(leftA, leftB) && areSimilar(rightA, rightB)
    case _ => false

def areEqual[A](treeA: BinaryTree[A], treeB: BinaryTree[A]): Boolean =
  (treeA, treeB) match
    case (Leaf, Leaf) => true
    case (Branch(valueA, leftA, rightA), Branch(valueB, leftB, rightB)) =>
      valueA == valueB && areEqual(leftA, leftB) && areEqual(rightA, rightB)
    case _ => false
```

## Естественное соответствие

Пусть \\(F = (T\_{1}, T\_{2}, ... , T\_{n})\\) — некоторый лес деревьев. 
Тогда бинарное дерево \\(B(F)\\), соответствующее \\(F\\), можно строго определить следующим образом.

- a) Если \\(n = 0\\), то \\(B(F)\\) пусто.
- b) Если \\(n > 0\\), то корень \\(B(F)\\) является корнем \\((T\_{1})\\); 
  \\(B(T\_{11}, T\_{12}, ... , T\_{1m})\\) является левым поддеревом дерева \\(B(F)\\), 
  где \\(T\_{11}, T\_{12}, ... , T\_{1m}\\) — поддеревья корня \\((T\_{1})\\); 
  \\(B(T\_{2}, ... , T\_{n})\\) является правым поддеревом дерева \\(B(F)\\).

Это приведение называется _естественным соответствием_ (_natural correspondence_) между лесом и бинарными деревьями.

## Применение

Двоичные деревья имеют множество применений.

Одной из специализаций двоичных деревьев являются двоичные деревья поиска, которые ускоряют поисковые приложения,
поскольку поддерживают поиск, вставку и удаление со средней временной сложностью _O(lg n)_.

Двоичные деревья также используются в компиляторах для обработки выражений с использованием деревьев выражений. 
Также их можно использовать для сжатия данных, таких как деревья кодирования Хаффмана. 

---

**Ссылки:**

- [Исходный код][source]
- [Donald E. Knuth - The Art of Computer Programming, section 2.3.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&client_id=springerlink&redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)

[source]: https://gitverse.ru/scalabook/algorithms
