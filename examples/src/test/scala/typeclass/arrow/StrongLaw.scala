package typeclass.arrow

import typeclass.common.Runner2
import typeclass.common.Runner2.run

trait StrongLaw extends ProfunctorLaw:
  def checkStrongLaw[=>:[_, _]: Strong: Runner2, A, B, C, D, E, F](gad: A =>: D, gab: A =>: B)(a: A, c: C, d: D)(using
      fcb: C => B,
      fcd: C => D,
      fba: B => A,
      fde: D => E,
      fef: E => F
  )(using PF: Strong[Function1]): Unit =
    val ins = summon[Strong[=>:]]
    import ins.*
    checkProfunctorLaw[=>:, A, B, C, D, E, F](gad)(a, c)
    assertEquals(
      run((a, c))(dimap(second(gad))(swapTuple[A, C])(swapTuple[C, D])),
      run((a, c))(first(gad)),
      "first' == dimap swap swap . second'"
    )
    assertEquals(
      run((c, a))(dimap(first(gad))(swapTuple[C, A])(swapTuple[D, C])),
      run((c, a))(second(gad)),
      "second' == dimap swap swap . first'"
    )

    assertEquals(
      run((a, c))(mapfst[A, D, (A, C)](gad)(_._1)),
      run((a, c))(mapsnd(first[A, D, C](gad))(_._1)),
      "lmap fst == rmap fst . first'"
    )
    assertEquals(
      run((c, a))(mapfst[A, D, (C, A)](gad)(_._2)),
      run((c, a))(mapsnd(second[A, D, C](gad))(_._2)),
      "lmap snd == rmap snd . second'"
    )

    assertEquals(
      run((a, c))(mapsnd(first[A, B, C](gab))(PF.second[C, D, B](fcd))),
      run((a, c))(mapfst(first[A, B, D](gab))(PF.second[C, D, A](fcd))),
      "lmap (second f) . first == rmap (second f) . first"
    )
    assertEquals(
      run((c, a))(mapsnd(second[A, B, C](gab))(PF.first[C, D, B](fcd))),
      run((c, a))(mapfst(second[A, B, D](gab))(PF.first[C, D, A](fcd))),
      "lmap (first f) . second == rmap (first f) . second"
    )

    assertEquals(
      run(((a, c), d))(first[(A, C), (B, C), D](first[A, B, C](gab))),
      run(((a, c), d))(
        dimap[(A, (C, D)), (B, (C, D)), ((A, C), D), ((B, C), D)](first[A, B, (C, D)](gab))(assoc)(unassoc)
      ),
      "first' . first' == dimap assoc unassoc . first'"
    )
    assertEquals(
      run((d, (c, a)))(second[(C, A), (C, B), D](second[A, B, C](gab))),
      run((d, (c, a)))(
        dimap[((D, C), A), ((D, C), B), (D, (C, A)), (D, (C, B))](second[A, B, (D, C)](gab))(unassoc)(assoc)
      ),
      "second' . second' == dimap unassoc assoc . second'"
    )

  private def swapTuple[X, Y]: ((X, Y)) => (Y, X) = _.swap

  private def assoc[A, B, C]: (((A, B), C)) => (A, (B, C)) = { case ((a, b), c) => (a, (b, c)) }

  private def unassoc[A, B, C]: ((A, (B, C))) => ((A, B), C) = { case (a, (b, c)) => ((a, b), c) }
