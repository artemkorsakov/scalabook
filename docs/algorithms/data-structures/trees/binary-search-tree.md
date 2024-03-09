# Двоичное дерево поиска

**Определение:**

Двоичное дерево является [двоичным деревом поиска](https://ru.wikipedia.org/wiki/%D0%94%D0%B2%D0%BE%D0%B8%D1%87%D0%BD%D0%BE%D0%B5_%D0%B4%D0%B5%D1%80%D0%B5%D0%B2%D0%BE_%D0%BF%D0%BE%D0%B8%D1%81%D0%BA%D0%B0), 
если значение в корневом узле больше или равно
всем значениям в левом поддереве и меньше или равно всем значениям в правом поддереве.
Это относится ко всем узлам, гарантируя, что элементы расположены в возрастающем порядке.

**Оценка:**

- _Поиск элемента_ (`search`)
  - **Время** - O(lg n)
  - **Память** - O(lg n)
- _Вставка элемента_ (`insert`)
  - **Время** - O(lg n)
  - **Память** - O(lg n)
- _Удаление элемента_ (`remove`)
  - **Время** - O(lg n)
  - **Память** - O(lg n)
  
**Код:**

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
```

---

**Ссылки:**

- [Donald E. Knuth - The Art of Computer Programming, section 2.3.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
