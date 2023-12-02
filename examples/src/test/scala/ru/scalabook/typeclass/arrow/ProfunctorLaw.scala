package ru.scalabook.typeclass.arrow

import cats.data.ValidatedNel
import ru.scalabook.typeclass.common.{Laws, Runner2}

trait ProfunctorLaw extends Laws:
  def checkProfunctorLaw[=>:[_, _]: Profunctor: Runner2, A, B, C, D, E, F](
      gad: A =>: D
  )(a: A, c: C)(using
      fcb: C => B,
      fba: B => A,
      fde: D => E,
      fef: E => F
  ): ValidatedNel[String, Unit] =
    check(
      Runner2[=>:].run(a)(
        Profunctor[=>:].dimap(gad)((a: A) => a)((d: D) => d)
      ) ==
        Runner2[=>:].run(a)(gad),
      "identity"
    ) combine
      check(
        Runner2[=>:].run(c)(
          Profunctor[=>:].dimap(Profunctor[=>:].dimap(gad)(fba)(fde))(fcb)(fef)
        ) ==
          Runner2[=>:].run(c)(
            Profunctor[=>:].dimap(gad)(fba compose fcb)(fef compose fde)
          ),
        "composite"
      )
end ProfunctorLaw
