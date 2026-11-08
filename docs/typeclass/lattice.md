# Решетки

Решёткоподобные структуры представляет структуры, которые могут напоминать кольца, но работают по другим законам.

Вместо `plus` (сложение) и `times` (умножение) здесь используются:

- **`meet`** (встреча/пересечение) — аналог логического **И** (AND) или операции взятия **минимума** («наибольшая нижняя граница»).
- **`join`** (объединение) — аналог логического **ИЛИ** (OR) или операции взятия **максимума** («наименьшая верхняя граница»).

**Свойства `meet` и `join`:**

- Они всегда ассоциативны, коммутативны и идемпотентны (`a ∧ a = a`).
- Каждый по отдельности образует **полурешётку** (Semilattice).

**Дополнительные элементы:**

- **`zero`**: если есть, то `join(a, zero) = a` (ноль — нижняя граница, объединение с ним не меняет элемент).
- **`one`**: если есть, то `meet(a, one) = a` (единица — верхняя граница).
- **Закон поглощения (Absorption)**: если есть оба, то `meet(a, join(a, b)) = join(a, meet(a, b)) = a`.
- **Дистрибутивность**: `meet` дистрибутивен относительно `join` и наоборот.
- **`imp` (импликация)**: специальная операция, означающая «если... то...».

### Таблица решёткоподобных структур

- [**JoinSemilattice**](lattice/join-semilattice.md): Полурешётка объединения (умеет только `join`).
- [**MeetSemilattice**](lattice/meet-semilattice.md): Полурешётка пересечения (умеет только `meet`).
- [**BoundedJoinSemilattice**](lattice/bounded-join-semilattice.md): Полурешётка объединения с нулём (нижней границей).
- [**BoundedMeetSemilattice**](lattice/bounded-meet-semilattice.md): Полурешётка пересечения с единицей (верхней границей).
- [**Lattice**](lattice/lattice.md): Полная решётка (есть и встреча, и объединение).
- [**DistributiveLattice**](lattice/distributive-lattice.md): Дистрибутивная решётка.  
  *Пример: множества с `∩` и `∪`.*
- [**BoundedLattice**](lattice/bounded-lattice.md): Решётка с границами (есть и 0, и 1).
- [**BoundedDistributiveLattice**](lattice/bounded-distributive-lattice.md): Дистрибутивная решётка с границами.
- [**Logic**](lattice/logic.md): **Общая логика**.
- [**DeMorgan**](lattice/de-morgan.md): **Алгебра Де Моргана**.
- [**Heyting**](lattice/heyting.md): **Алгебра Гейтинга**. Решётка с импликацией.  
  *Пример: логика без закона исключённого третьего.*
- [**GenBool**](lattice/gen-bool.md): **Общая Булева алгебра**.
- [**Bool**](lattice/bool.md): **Булева алгебра**. Классическая логика с законом исключённого третьего (`a || !a == true`).  
  *Пример: `true/false`, битовые маски.*

## Схема группы

```mermaid
---
title: Решёткоподобные структуры
---
classDiagram
  class JoinSemilattice~A~ {
    +join(lhs: A, rhs: A) A
  }
  class BoundedJoinSemilattice~A~ {
    +zero: A
    +isZero(a: A)(using Equal[A]) Boolean
  }
  class MeetSemilattice~A~ {
    +meet(lhs: A, rhs: A) A
  }
  class BoundedMeetSemilattice~A~ {
    +one: A
    +isOne(a: A)(using Equal[A]) Boolean
  }
  class Lattice~A~ {
    +dual() Lattice~A~
  }
  class DistributiveLattice~A~
  class BoundedLattice~A~
  class BoundedDistributiveLattice~A~
  class Logic~A~ {
    +and(a: A, b: A) A
    +or(a: A, b: A) A
    +not(a: A) A
    +xor(a: A, b: A) A
    +nand(a: A, b: A) A
    +nor(a: A, b: A) A
    +nxor(a: A, b: A) A
  }
  class DeMorgan~A~ {
    +imp(a: A, b: A) A
  }
  class Heyting~A~ {
    +and(a: A, b: A) A
    +or(a: A, b: A) A
    +imp(a: A, b: A) A
    +complement(a: A) A
    +xor(a: A, b: A) A
    +nand(a: A, b: A) A
    +nor(a: A, b: A) A
    +nxor(a: A, b: A) A
  }
  class GenBool~A~ {
    +and(a: A, b: A) A
    +or(a: A, b: A) A
    +zero: A
    +without(a: A, b: A) A
    +xor(a: A, b: A) A
  }
  class Bool~A~

  JoinSemilattice <|-- Lattice
  JoinSemilattice <|-- BoundedJoinSemilattice
  MeetSemilattice <|-- Lattice
  MeetSemilattice <|-- BoundedMeetSemilattice
  BoundedJoinSemilattice <|-- BoundedLattice
  BoundedMeetSemilattice <|-- BoundedLattice
  Lattice <|-- BoundedLattice
  BoundedLattice <|-- BoundedDistributiveLattice
  Lattice <|-- DistributiveLattice
  DistributiveLattice <|-- BoundedDistributiveLattice
  BoundedDistributiveLattice <|-- Logic
  Logic <|-- DeMorgan
  BoundedDistributiveLattice <|-- Heyting
  BoundedJoinSemilattice <|-- GenBool
  DistributiveLattice <|-- GenBool
  DeMorgan <|-- Bool
  Heyting <|-- Bool
  GenBool <|-- Bool
```

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Cats](https://typelevel.org/cats/)

[sample]: https://gitverse.ru/scalabook/typeclasses
