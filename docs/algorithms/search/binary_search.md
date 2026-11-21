# Бинарный поиск

> [Двоичный (бинарный) поиск](https://ru.wikipedia.org/wiki/%D0%94%D0%B2%D0%BE%D0%B8%D1%87%D0%BD%D1%8B%D0%B9_%D0%BF%D0%BE%D0%B8%D1%81%D0%BA) 
> (также известен как **метод деления пополам** или **дихотомия**) — 
> классический алгоритм поиска элемента в отсортированном массиве (векторе), использующий дробление массива на половины.

**Алгоритм и пояснение:**

В функциональном программировании сложность представляет деление пополам связанного списка.
Для того, чтобы вычислить длину связанного списка, нужно пройти по всем элементам, а это занимает линейное время.
Затем нужно будет найти средний элемент, что также занимает линейное время. 
Итого, если длина списка равна n, то вычисление половины списка займет \\(\frac{3n}{2}\\) операций.

Попробуем вычислить половину списка за \\(\frac{n}{2}\\) операций.
Определим алгоритм "гонки", вычисляющий половину списка за \\(\frac{n}{2}\\) операций.

- **Шаг 1** (Инициализация значений). Определим два списка: `list` и `subList`, где второй всегда будет подсписком первого.
  Первоначальные значения этих параметров: исходный и пустой списки.
- **Шаг 2** (Определение отсечения). Определим два списка: `oneStep` и `twoStep`, где в первом списке будем отсекать по одному элементу из `list`, 
  а во втором списке - по два элемента из `list`.  Первоначальные значения этих параметров: исходный список `list`.
- **Шаг 3** (Проверка условия). Если `twoStep` или он же без головного элемента равен `subList`, то в качестве половины возвращаем `oneStep`.
  Иначе отсекаем из `oneStep` один элемент, а из `twoStep` - два.

`oneStep` будет содержать ровно половину элементов между `list` и `subList`, т.к. если к тому моменту, когда `twoStep` станет равен `subList`,
из него будет отсечено `2k` элементов, стоящих между `list` и `subList`, то тогда в `oneStep` будет отсечено ровно `k` элементов, т.е. половина.

Далее используем стандартный алгоритм бинарного поиска:

- **Шаг 1** (Базовые условия). В методе `search` "стягиваем" два списка: исходный и пустой.
- **Шаг 2** (Поиск середины). Находим `middle` - середину этих двух списков - список, содержащий половину элементов исходного списка, по алгоритму выше.
- **Шаг 3** (Проверка условия). Сравнение ключа с головным элементом `middle`.
    - Если исходный ключ меньше головного элемента `middle`, то продолжаем поиск в левой половине ("стягиваем" исходный список и `middle`).
    - Если исходный ключ больше головного элемента `middle`, то продолжаем поиск в правой половине ("стягиваем" `middle` без заглавного элемента и пустой список).
    - Если исходный ключ равен головному элементу `middle`, то возвращаем этот элемент.
- **Шаг 4** (Окончание). Если в процессе "стягивания" два списка стали равны, то искомый элемент не найден.

**Пример:**

Рассмотрим пример вычисления бинарного поиска для списка `List(1, 3, 5, 7, 9)` и ключа `key = 3`.

Пример вычисления:

- Инициализация: `list = List(1, 3, 5, 7, 9), subList = List()`, `key = 3`
- Поиск половины: 
    - `oneStep = List(1, 3, 5, 7, 9), twoSteps = List(1, 3, 5, 7, 9) =>` 
    - `oneStep = List(3, 5, 7, 9), twoSteps = List(5, 7, 9) =>` 
    - `oneStep = List(5, 7, 9), twoSteps = List(9)`, рекурсия завершена: `twoSteps` без головного элемента равен `subList`
    - Половина - `List(5, 7, 9)`
- Проверка условия: `3` меньше `5`, поэтому продолжаем для списков `list = List(1, 3, 5, 7, 9), subList = List(5, 7, 9)`.
- Поиск половины: 
    - `oneStep = List(1, 3, 5, 7, 9), twoSteps = List(1, 3, 5, 7, 9)` 
    - `oneStep = List(3, 5, 7, 9), twoSteps = List(5, 7, 9)`, рекурсия завершена: `twoSteps` равен `subList`.
    - Половина - `List(3, 5, 7, 9)`
- Исходный ключ равен головному элементу "половины", поэтому возвращаем ответ: `3`.

**Оценка:**

- **Время**:
    - Худшее - \\(O(\log n)\\)
    - Лучшее - \\(O(1)\\)
    - Среднее - \\(O(\log n)\\)
- **Память** - \\(O(1)\\)

**Код:**

```dotty
import scala.annotation.tailrec
import scala.math.Ordered.orderingToOrdered

def binarySearch[A: Ordering](list: List[A], key: A): Option[A] =
  @tailrec
  def search(lst: List[A], sub: List[A]): Option[A] =
    if lst == sub then None
    else
      middle(lst, sub) match
        case Nil => None
        case head :: next =>
          if key < head then search(lst, head :: next)
          else if key > head then search(next, sub)
          else Some(head)

  def middle(lst: List[A], sub: List[A]): List[A] =
    @tailrec
    def race(oneStep: List[A], twoSteps: List[A]): List[A] =
      if twoSteps.nonEmpty && twoSteps != sub && twoSteps.tail != sub then
        race(oneStep.drop(1), twoSteps.drop(2))
      else oneStep

    race(lst, lst)

  search(list, Nil)
end binarySearch

binarySearch((1 to 100).toList, 45)
// Some(45)
```

---

**Ссылки:**

- [Исходный код][sample]
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Scalacaster - Search algorithms](https://github.com/vkostyukov/scalacaster/tree/master/src/search)
- [Алгоритмика - Бинарный поиск](https://ru.algorithmica.org/cs/interactive/binary-search/)
- [Алгоритмика - Бинарный поиск по ответу](https://ru.algorithmica.org/cs/interactive/answer-search/)

[sample]: https://gitverse.ru/scalabook/algorithms
