package ru.scalabook.books.tlpis

object EquivHList:

  // HNil === HNil
  given Equiv[HNil.type] with
    def equiv(a: HNil.type, b: HNil.type) = true

  // заданы Equiv для заголовка и остатка,
  //   (h1 :: t1) === (h2 :: t2) тогда, когда
  //   (h1 === h2) и (t1 === t2)
  given equivHCons[H, T <: HList](using
      equivH: Equiv[H],
      equivT: Equiv[T]
  ): Equiv[HCons[H, T]] with
    def equiv(a: HCons[H, T], b: HCons[H, T]): Boolean =
      equivH.equiv(a.head, b.head) && equivT.equiv(a.tail, b.tail)

  // дано:
  //   - тип T, конвертируемый в HList типа HL
  //   - экземпляр Equiv для HL
  // мы можем создать Equiv[T] приведя T к HL и используя Equiv[HL]
  given equivHListable[T, HL <: HList](using
      toHList: T => HL,
      equivHL: Equiv[HL]
  ): Equiv[T] with
    def equiv(a: T, b: T): Boolean =
      equivHL.equiv(toHList(a), toHList(b))

  extension [A](a1: A)
    def ===(a2: A): Boolean = Equiv.universal.equiv(a1, a2)

    def !==(a2: A): Boolean = !Equiv.universal.equiv(a1, a2)
