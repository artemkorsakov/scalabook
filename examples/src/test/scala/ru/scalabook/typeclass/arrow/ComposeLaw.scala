package ru.scalabook.typeclass.arrow

import cats.data.ValidatedNel
import ru.scalabook.typeclass.common.{OldLaws, Runner2}

trait ComposeLaw extends OldLaws:
  def checkComposeLaw[=>:[_, _]: Compose: Runner2, A, B, C, D](
      ab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A,
      f2: A =>: A,
      f3: A =>: A
  )(a: A): ValidatedNel[String, Unit] =
    check(
      Runner2[=>:].run(a)(
        Compose[=>:].compose(cd, Compose[=>:].compose(bc, ab))
      ) ==
        Runner2[=>:].run(a)(
          Compose[=>:].compose(Compose[=>:].compose(cd, bc), ab)
        ),
      "Associativity"
    ) combine
      check(
        Runner2[=>:].run(a)(
          Compose[=>:].plus.plus(Compose[=>:].plus.plus(f1, f2), f3)
        ) ==
          Runner2[=>:].run(a)(
            Compose[=>:].plus.plus(f1, Compose[=>:].plus.plus(f2, f3))
          ),
        "Associativity"
      )
end ComposeLaw
