package typeclass.arrow

trait StrongLaw extends ProfunctorLaw:
  def checkStrongLaw[=>:[_, _]: Strong, A, B, C, D, E, F](
      gad: A =>: D,
      gab: A =>: B
  )(
      runAD: A =>: D => D,
      runCF: C =>: F => F,
      runACDC: (A, C) =>: (D, C) => (D, C),
      runCACD: (C, A) =>: (C, D) => (C, D),
      runACD: (A, C) =>: D => D,
      runCAD: (C, A) =>: D => D,
      runACBD: (A, C) =>: (B, D) => (B, D),
      runCADB: (C, A) =>: (D, B) => (D, B),
      runACDBCD: ((A, C), D) =>: ((B, C), D) => ((B, C), D),
      runDCADCB: (D, (C, A)) =>: (D, (C, B)) => (D, (C, B))
  )(using
      fcb: C => B,
      fcd: C => D,
      fba: B => A,
      fde: D => E,
      fef: E => F
  )(using PF: Strong[Function1]): Unit =
    val ins = summon[Strong[=>:]]
    import ins.*
    checkProfunctorLaw[=>:, A, B, C, D, E, F](gad)(runAD, runCF)
    assertEquals(
      runACDC(dimap(second(gad))(swapTuple[A, C])(swapTuple[C, D])),
      runACDC(first(gad)),
      "first' == dimap swap swap . second'"
    )
    assertEquals(
      runCACD(dimap(first(gad))(swapTuple[C, A])(swapTuple[D, C])),
      runCACD(second(gad)),
      "second' == dimap swap swap . first'"
    )

    assertEquals(
      runACD(mapfst[A, D, (A, C)](gad)(_._1)),
      runACD(mapsnd(first[A, D, C](gad))(_._1)),
      "lmap fst == rmap fst . first'"
    )
    assertEquals(
      runCAD(mapfst[A, D, (C, A)](gad)(_._2)),
      runCAD(mapsnd(second[A, D, C](gad))(_._2)),
      "lmap snd == rmap snd . second'"
    )

    assertEquals(
      runACBD(mapsnd(first[A, B, C](gab))(PF.second[C, D, B](fcd))),
      runACBD(mapfst(first[A, B, D](gab))(PF.second[C, D, A](fcd))),
      "lmap (second f) . first == rmap (second f) . first"
    )
    assertEquals(
      runCADB(mapsnd(second[A, B, C](gab))(PF.first[C, D, B](fcd))),
      runCADB(mapfst(second[A, B, D](gab))(PF.first[C, D, A](fcd))),
      "lmap (first f) . second == rmap (first f) . second"
    )

    assertEquals(
      runACDBCD(first[(A, C), (B, C), D](first[A, B, C](gab))),
      runACDBCD(dimap[(A, (C, D)), (B, (C, D)), ((A, C), D), ((B, C), D)](first[A, B, (C, D)](gab))(assoc)(unassoc)),
      "first' . first' == dimap assoc unassoc . first'"
    )
    assertEquals(
      runDCADCB(second[(C, A), (C, B), D](second[A, B, C](gab))),
      runDCADCB(dimap[((D, C), A), ((D, C), B), (D, (C, A)), (D, (C, B))](second[A, B, (D, C)](gab))(unassoc)(assoc)),
      "second' . second' == dimap unassoc assoc . second'"
    )

  private def swapTuple[X, Y]: Tuple2[X, Y] => Tuple2[Y, X] = _.swap

  private def assoc[A, B, C]: (((A, B), C)) => (A, (B, C)) = { case ((a, b), c) => (a, (b, c)) }

  private def unassoc[A, B, C]: ((A, (B, C))) => ((A, B), C) = { case (a, (b, c)) => ((a, b), c) }
