package ru.scalabook.books.tlpis

sealed trait HList

final case class HCons[H, T <: HList](head: H, tail: T) extends HList:
  def ::[A](v: A): HList = HCons(v, this)

sealed class HNil extends HList:
  def ::[A](v: A): HList = HCons(v, this)

// алиасы для построения типов HList и для сопоставления с шаблоном
object HList:
  type ::[H, T <: HList] = HCons[H, T]
  val :: : HCons.type = HCons
