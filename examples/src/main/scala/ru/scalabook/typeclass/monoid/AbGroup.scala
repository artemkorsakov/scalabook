package ru.scalabook.typeclass.monoid

trait AbGroup[A] extends Group[A], CMonoid[A]
