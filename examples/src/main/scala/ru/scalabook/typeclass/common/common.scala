package ru.scalabook.typeclass.common

import io.github.iltotore.iron.:|
import io.github.iltotore.iron.constraint.all.Greater

type PositiveInt = Int :| Greater[0]
