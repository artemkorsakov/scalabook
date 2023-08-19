package ru.scalabook.books.tlpis

trait Fold[-Elem, Value]:
  type Apply[E <: Elem, V <: Value] <: Value
