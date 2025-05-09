{%
showPrev = true
prevPage = binary-tree.html
showNext = true
nextPage = ../heap.html
%}

# Двоичное дерево поиска

**Определение:**

Двоичное дерево является [двоичным деревом поиска](https://ru.wikipedia.org/wiki/%D0%94%D0%B2%D0%BE%D0%B8%D1%87%D0%BD%D0%BE%D0%B5_%D0%B4%D0%B5%D1%80%D0%B5%D0%B2%D0%BE_%D0%BF%D0%BE%D0%B8%D1%81%D0%BA%D0%B0), 
если значение в корневом узле больше или равно
всем значениям в левом поддереве и меньше или равно всем значениям в правом поддереве.
Это относится ко всем узлам, гарантируя, что элементы расположены в возрастающем порядке.

**Оценка:**

- _Поиск элемента_ (`search`)
    - **Время** - O(log n)
    - **Память** - O(log n)
- _Вставка элемента_ (`insert`)
    - **Время** - O(log n)
    - **Память** - O(log n)
- _Обновление значения элемента_ (`update`)
    - **Время** - O(log n)
    - **Память** - O(log n) 
- _Удаление элемента_ (`remove`)
    - **Время** - O(log n)
    - **Память** - O(log n)
  
**Код:**

В следующем коде определены методы для работы с двоичным деревом поиска (Binary Search Tree, BST).

Основные публичные методы, определенные в `extension` для `Dictionary[A]`, включают:

1. `insert(key: String, value: A): Dictionary[A]` - Вставляет новую пару ключ-значение в дерево. 

2. `searchKey(key: String): Option[A]` - Ищет значение по ключу. Если ключ найден, возвращает `Some(value)`, иначе - `None`.

3. `updateValue(key: String, value: A): Dictionary[A]` - Обновляет значение по ключу. 
   Если ключ не найден, он вставляет новую пару ключ-значение.

4. `remove(key: String): Dictionary[A]` - Удаляет пару ключ-значение по ключу. 
   Если ключ не найден, дерево остается неизменным.

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

## Методы двоичного дерева

**Код:**

В следующем коде есть несколько методов, которые "наследуются" от [двоичного дерева](binary-tree.md).

```dotty
object BinarySearchTree:  
  extension [A](dict: Dictionary[A])    
    def isEmpty: Boolean = dict.isEmpty

    def size: Int = dict.size

    def height: Int = dict.height

end BinarySearchTree
```

## Параметры дерева поиска

**Код:**

В следующем коде есть несколько методов, которые работают с бинарными деревьями и словарями, основанными на этих деревьях. 

- `isValid` - метод проверяет, является ли `Dictionary` валидным двоичным деревом поиска. 
  Он проверяет, что для каждого узла все ключи в левом поддереве меньше ключа узла, 
  а все ключи в правом поддереве больше ключа узла.
- `isBalanced` - Этот метод проверяет, является ли `Dictionary` сбалансированным двоичным деревом. 
  Он проверяет, что для каждого узла разница высот левого и правого поддеревьев не более 1, 
  и рекурсивно проверяет левое и правое поддеревья.

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

**Ссылки:**

- [Donald E. Knuth - The Art of Computer Programming, section 2.3.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&client_id=springerlink&redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
