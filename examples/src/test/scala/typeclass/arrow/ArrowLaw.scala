package typeclass.arrow

trait ArrowLaw extends StrongLaw, SplitLaw, CategoryLaw:
  def checkArrowLaw[=>:[_, _]: Arrow, A, B, C, D, E, F](
      gad: A =>: D,
      gab: A =>: B,
      bc: B =>: C,
      cd: C =>: D,
      f1: A =>: A,
      f2: A =>: A,
      f3: A =>: A
  )(
      runAA: A =>: A => A,
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
  )(using PF: Arrow[Function1]): Unit =
    checkStrongLaw[=>:, A, B, C, D, E, F](gad, gab)(
      runAD,
      runCF,
      runACDC,
      runCACD,
      runACD,
      runCAD,
      runACBD,
      runCADB,
      runACDBCD,
      runDCADCB
    )
    checkSplitLaw[=>:, A, B, C, D](gab, bc, cd, f1, f2, f3)(runAA, runAD)
    //checkCategoryLaw[=>:, A, B, C, D](gab, bc, cd, f1, f2, f3)(runAA, runAD)
